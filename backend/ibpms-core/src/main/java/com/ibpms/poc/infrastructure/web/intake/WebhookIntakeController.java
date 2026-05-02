package com.ibpms.poc.infrastructure.web.intake;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * REST controller for O365/external webhook intake (US-004).
 * Exposes the public endpoint for receiving incoming messages.
 */
@RestController
@RequestMapping("/intake/webhook")
public class WebhookIntakeController {

    private static final Logger log = LoggerFactory.getLogger(WebhookIntakeController.class);
    private final WebhookIntakeService intakeService;
    private final RabbitTemplate rabbitTemplate;

    public WebhookIntakeController(WebhookIntakeService intakeService, RabbitTemplate rabbitTemplate) {
        this.intakeService = intakeService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Receives a webhook POST from an external system (O365, APIM, etc.).
     * CA-17: ACK sub-segundo — processing is asynchronous (published to RabbitMQ).
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String hmacSignature,
            @RequestParam String messageId,
            @RequestParam String senderEmail,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String tenantId,
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) MultipartFile attachment) {

        log.info("Webhook received: messageId=[{}], sender=[{}]", messageId, senderEmail);

        // HMAC validation (CA-10) - Synchronous to reject immediately
        String bodyForHmac = rawBody != null ? rawBody : "";
        if (!intakeService.validateHmacSignature(bodyForHmac, hmacSignature)) {
            log.warn("HMAC validation failed for messageId=[{}]", messageId);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "INVALID_SIGNATURE", "message", "HMAC signature validation failed."));
        }

        // Idempotency check (CA-1) - Synchronous
        if (intakeService.isIdempotent(messageId)) {
            log.info("Duplicate webhook detected for messageId=[{}]. Returning silent 200.", messageId);
            return ResponseEntity.ok(Map.of("status", "IDEMPOTENT", "message", "Duplicate message; already processed."));
        }

        // Auto-responder block (CA-2) - Synchronous
        if (intakeService.isAutoResponder(senderEmail)) {
            log.warn("Auto-responder blocked: [{}]", senderEmail);
            return ResponseEntity.status(400).body(Map.of("status", "AUTO_RESPONDER_BLOCKED", "message", "System accounts (no-reply, mailer-daemon) are not allowed."));
        }

        // Build payload
        byte[] attachmentBytes = null;
        String attachmentFileName = null;
        if (attachment != null && !attachment.isEmpty()) {
            try {
                attachmentBytes = attachment.getBytes();
                attachmentFileName = attachment.getOriginalFilename();
            } catch (IOException e) {
                log.error("Failed to read attachment: {}", e.getMessage());
            }
        }

        WebhookPayload payload = new WebhookPayload(
                messageId, senderEmail, subject, rawBody,
                attachmentBytes, attachmentFileName, tenantId
        );

        rabbitTemplate.convertAndSend("ibpms.integrations.webhook", payload);

        return ResponseEntity.accepted().body(Map.of(
                "status", "ACCEPTED",
                "message", "Webhook payload queued for processing.",
                "messageId", messageId
        ));
    }
}
