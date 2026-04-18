package com.ibpms.poc.infrastructure.web.intake;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookPayload;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public WebhookIntakeController(WebhookIntakeService intakeService) {
        this.intakeService = intakeService;
    }

    /**
     * Receives a webhook POST from an external system (O365, APIM, etc.).
     * CA-17: ACK sub-segundo — processing is synchronous but optimized.
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

        // HMAC validation (CA-10)
        String bodyForHmac = rawBody != null ? rawBody : "";
        if (!intakeService.validateHmacSignature(bodyForHmac, hmacSignature)) {
            log.warn("HMAC validation failed for messageId=[{}]", messageId);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "INVALID_SIGNATURE", "message", "HMAC signature validation failed."));
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

        WebhookResponse response = intakeService.processIncomingWebhook(payload);

        return ResponseEntity.status(response.httpStatus())
                .body(Map.of(
                        "status", response.status(),
                        "message", response.message(),
                        "processInstanceId", response.processInstanceId() != null ? response.processInstanceId() : "",
                        "transactionId", response.transactionId() != null ? response.transactionId().toString() : ""
                ));
    }
}
