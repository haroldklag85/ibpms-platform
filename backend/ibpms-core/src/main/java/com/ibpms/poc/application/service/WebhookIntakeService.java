package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.model.WebhookTransaction;
import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.domain.port.AllowedDomainRepository;
import com.ibpms.poc.domain.port.ClamAvScanner;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import com.ibpms.poc.domain.port.WebhookTransactionRepository;
import com.ibpms.poc.infrastructure.config.WebhookProperties;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Core orchestrator for Webhook Intake processing (US-004).
 * Sequential validation pipeline: Idempotency → Auto-responder block →
 * Payload validation → Domain whitelist → HMAC/Bearer → Size limit →
 * ClamAV scan → Pre-Triage instantiation.
 */
@Service
public class WebhookIntakeService {

    private static final Logger log = LoggerFactory.getLogger(WebhookIntakeService.class);

    /** Regex patterns for system/auto-responder email addresses (CA-2). */
    private static final List<Pattern> AUTO_RESPONDER_PATTERNS = List.of(
            Pattern.compile("^no-reply@.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^noreply@.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^mailer-daemon@.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^postmaster@.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^bounce@.*", Pattern.CASE_INSENSITIVE)
    );

    private final WebhookTransactionRepository transactionRepo;
    private final OrphanPayloadRepository orphanRepo;
    private final AllowedDomainRepository domainRepo;
    private final ClamAvScanner clamAvScanner;
    private final RuntimeService runtimeService;
    private final WebhookProperties webhookProperties;

    private final TriageTaskRepository triageTaskRepo;

    public WebhookIntakeService(
            WebhookTransactionRepository transactionRepo,
            OrphanPayloadRepository orphanRepo,
            AllowedDomainRepository domainRepo,
            TriageTaskRepository triageTaskRepo,
            ClamAvScanner clamAvScanner,
            RuntimeService runtimeService,
            WebhookProperties webhookProperties) {
        this.transactionRepo = transactionRepo;
        this.orphanRepo = orphanRepo;
        this.domainRepo = domainRepo;
        this.triageTaskRepo = triageTaskRepo;
        this.clamAvScanner = clamAvScanner;
        this.runtimeService = runtimeService;
        this.webhookProperties = webhookProperties;
    }

    /**
     * Main entry point for processing an incoming webhook payload.
     *
     * @param payload the parsed webhook payload
     * @return response indicating processing outcome
     */
    @Transactional
    public WebhookResponse processIncomingWebhook(WebhookPayload payload) {
        log.info("Processing incoming webhook, messageId=[{}]", payload.messageId());

        // Step 1: Idempotency check (CA-1)
        if (transactionRepo.existsByMessageId(payload.messageId())) {
            log.info("Duplicate webhook detected for messageId=[{}]. Returning silent 200.", payload.messageId());
            return WebhookResponse.idempotent();
        }

        // Step 2: Auto-responder block (CA-2)
        if (isAutoResponder(payload.senderEmail())) {
            log.warn("Auto-responder blocked: [{}]", payload.senderEmail());
            return WebhookResponse.rejected("AUTO_RESPONDER_BLOCKED",
                    "System accounts (no-reply, mailer-daemon) are not allowed.");
        }

        // Step 3: Extract domain and validate whitelist (CA-4)
        String domain = extractDomain(payload.senderEmail());
        String tenantId = payload.tenantId() != null ? payload.tenantId() : "default";
        if (!domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(domain, tenantId)) {
            log.warn("Domain not whitelisted: [{}] for tenant [{}]", domain, tenantId);
            persistOrphanPayload(payload, "UNAUTHORIZED");
            return WebhookResponse.forbidden("Domain not authorized: " + domain);
        }

        // Step 4: Payload size validation (CA-7)
        long payloadSize = calculatePayloadSize(payload);
        if (payloadSize > webhookProperties.getPayload().getMaxSizeBytes()) {
            log.warn("Payload exceeds size limit: {} > {}", payloadSize,
                    webhookProperties.getPayload().getMaxSizeBytes());
            return WebhookResponse.tooLarge(payloadSize, webhookProperties.getPayload().getMaxSizeBytes());
        }

        // Step 5: ClamAV anti-malware scan (CA-11)
        if (payload.attachmentBytes() != null && payload.attachmentBytes().length > 0) {
            ClamAvScanner.ScanResult scanResult = clamAvScanner.scan(
                    payload.attachmentBytes(),
                    payload.attachmentFileName() != null ? payload.attachmentFileName() : "unknown"
            );

            switch (scanResult) {
                case INFECTED:
                    log.error("MALWARE DETECTED in webhook attachment from [{}]!", payload.senderEmail());
                    persistOrphanPayload(payload, "MALWARE_QUARANTINE");
                    persistTransaction(payload, "QUARANTINED", "MALWARE_DETECTED");
                    return WebhookResponse.malwareDetected(payload.attachmentFileName());

                case UNAVAILABLE:
                    log.warn("ClamAV unavailable. Fail-secure: rejecting payload from [{}].", payload.senderEmail());
                    persistTransaction(payload, "REJECTED", "SCANNER_UNAVAILABLE");
                    return WebhookResponse.scannerUnavailable();

                case CLEAN:
                    log.info("Attachment scanned CLEAN.");
                    break;
            }
        }

        // Step 6: Persist transaction and create Pre-Triage task (CA-8/CA-9)
        try {
            Map<String, Object> camundaVars = Map.of(
                    "sender", payload.senderEmail(),
                    "subject", payload.subject() != null ? payload.subject() : "",
                    "messageId", payload.messageId(),
                    "attachmentCount", payload.attachmentBytes() != null ? 1 : 0
            );

            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    webhookProperties.getPreTriage().getProcessDefinitionKey(),
                    payload.messageId(),
                    camundaVars
            );

            WebhookTransaction tx = persistTransaction(payload, "PROCESSED", null);
            tx.setCamundaProcessInstanceId(instance.getProcessInstanceId());
            transactionRepo.save(tx);

            // DB Record for human triage (CA-8/CA-9 hybrid approach)
            TriageTask triageTask = TriageTask.builder()
                    .id(UUID.randomUUID())
                    .camundaProcessInstanceId(instance.getProcessInstanceId())
                    .messageId(payload.messageId())
                    .senderEmail(payload.senderEmail())
                    .subject(payload.subject())
                    .attachmentCount(payload.attachmentBytes() != null ? 1 : 0)
                    .status("PENDING")
                    .slaDeadline(ZonedDateTime.now().plusHours(4)) // Enforced baseline 4-hour SLA
                    .createdAt(ZonedDateTime.now())
                    .updatedAt(ZonedDateTime.now())
                    .build();
            triageTaskRepo.save(triageTask);

            log.info("Pre-Triage process instantiated: processInstanceId=[{}], messageId=[{}]",
                    instance.getProcessInstanceId(), payload.messageId());

            return WebhookResponse.accepted(instance.getProcessInstanceId(), tx.getId());

        } catch (Exception e) {
            log.error("Failed to instantiate Pre-Triage process for messageId=[{}]: {}",
                    payload.messageId(), e.getMessage());
            persistTransaction(payload, "REJECTED", "ENGINE_FAILURE");
            return WebhookResponse.engineFailure(e.getMessage());
        }
    }

    /**
     * Validates HMAC signature against the shared secret (CA-10).
     */
    public boolean validateHmacSignature(String rawBody, String signatureHeader) {
        if (!"HMAC".equalsIgnoreCase(webhookProperties.getSecurity().getMode())) {
            return true; // Bearer mode — skip HMAC validation
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookProperties.getSecurity().getHmacSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hmacBytes);
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("HMAC validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Purges orphan payloads older than 30 days (CA-13).
     */
    @Transactional
    public void purgeExpiredOrphanPayloads() {
        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(30);
        log.info("Purging orphan payloads older than {}", cutoff);
        orphanRepo.deleteByCreatedAtBefore(cutoff);
    }

    // ========== Private helpers ==========

    private boolean isAutoResponder(String email) {
        if (email == null) return true;
        return AUTO_RESPONDER_PATTERNS.stream().anyMatch(p -> p.matcher(email).matches());
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return "";
        return email.substring(email.indexOf("@"));
    }

    private long calculatePayloadSize(WebhookPayload payload) {
        long size = 0;
        if (payload.rawBody() != null) {
            size += payload.rawBody().getBytes(StandardCharsets.UTF_8).length;
        }
        if (payload.attachmentBytes() != null) {
            size += payload.attachmentBytes().length;
        }
        return size;
    }

    private WebhookTransaction persistTransaction(WebhookPayload payload, String status, String reason) {
        WebhookTransaction tx = WebhookTransaction.builder()
                .id(UUID.randomUUID())
                .messageId(payload.messageId())
                .senderEmail(payload.senderEmail())
                .senderDomain(extractDomain(payload.senderEmail()))
                .subject(payload.subject())
                .status(status)
                .rejectionReason(reason)
                .createdAt(ZonedDateTime.now())
                .build();
        return transactionRepo.save(tx);
    }

    private void persistOrphanPayload(WebhookPayload payload, String errorType) {
        String fileHash = null;
        if (payload.attachmentBytes() != null) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(payload.attachmentBytes());
                fileHash = Base64.getEncoder().encodeToString(hash);
            } catch (Exception ignored) {}
        }

        OrphanPayload orphan = OrphanPayload.builder()
                .id(UUID.randomUUID())
                .rawPayload(payload.rawBody())
                .errorType(errorType)
                .fileHashSha256(fileHash)
                .fileName(payload.attachmentFileName())
                .fileSizeBytes(payload.attachmentBytes() != null ? (long) payload.attachmentBytes().length : null)
                .senderEmail(payload.senderEmail())
                .createdAt(ZonedDateTime.now())
                .build();
        orphanRepo.save(orphan);
    }

    // ========== DTOs ==========

    /**
     * Inbound payload structure for webhook intake.
     */
    public record WebhookPayload(
            String messageId,
            String senderEmail,
            String subject,
            String rawBody,
            byte[] attachmentBytes,
            String attachmentFileName,
            String tenantId
    ) {}

    /**
     * Response encapsulating the processing outcome.
     */
    public record WebhookResponse(
            int httpStatus,
            String status,
            String message,
            String processInstanceId,
            UUID transactionId
    ) {
        public static WebhookResponse idempotent() {
            return new WebhookResponse(200, "IDEMPOTENT", "Duplicate message; already processed.", null, null);
        }

        public static WebhookResponse rejected(String reason, String message) {
            return new WebhookResponse(400, reason, message, null, null);
        }

        public static WebhookResponse forbidden(String message) {
            return new WebhookResponse(403, "DOMAIN_NOT_AUTHORIZED", message, null, null);
        }

        public static WebhookResponse tooLarge(long actual, long max) {
            return new WebhookResponse(413, "PAYLOAD_TOO_LARGE",
                    "Payload size " + actual + " exceeds limit of " + max + " bytes.", null, null);
        }

        public static WebhookResponse malwareDetected(String fileName) {
            return new WebhookResponse(422, "MALWARE_DETECTED",
                    "File '" + fileName + "' rejected by security policy.", null, null);
        }

        public static WebhookResponse scannerUnavailable() {
            return new WebhookResponse(503, "SCANNER_UNAVAILABLE",
                    "Anti-malware scanner is unavailable. Payload rejected (fail-secure).", null, null);
        }

        public static WebhookResponse accepted(String processInstanceId, UUID transactionId) {
            return new WebhookResponse(200, "ACCEPTED",
                    "Webhook processed. Pre-Triage task created.", processInstanceId, transactionId);
        }

        public static WebhookResponse engineFailure(String detail) {
            return new WebhookResponse(500, "ENGINE_FAILURE",
                    "Failed to instantiate process: " + detail, null, null);
        }
    }
}
