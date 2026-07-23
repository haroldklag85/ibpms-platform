package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.ProcesarEmailWebhookUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;

/**
 * Adaptador Driving — Controlador REST HTTP para Graph API o Azure Logic Apps.
 */
@Deprecated(since = "v1.0.0", forRemoval = true)
@RestController
@RequestMapping("/inbound/email-webhook")
@Traceability(US = "US-013", CA = {"CA-01"})
public class EmailWebhookController {

    private final ProcesarEmailWebhookUseCase webhookUseCase;

    @Value("${app.webhook.ms-graph.client-state:secreto-compartido-m365}")
    private String expectedClientState;

    public EmailWebhookController(ProcesarEmailWebhookUseCase webhookUseCase) {
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receiveEmailNotification(
            @RequestHeader(value = "ClientState", required = false) String clientState,
            @RequestBody Map<String, Object> payload) {
        
        // SECURITY GATE: Legacy endpoint deprecado.
        // Todas las integraciones deben usar POST /api/v1/intake/webhook (WebhookIntakeService)
        return ResponseEntity.status(HttpStatus.GONE) // HTTP 410 Gone
                .body(Map.of(
                    "error", "ENDPOINT_DEPRECATED",
                    "message", "This endpoint has been deprecated. Use POST /api/v1/intake/webhook with HMAC validation.",
                    "migration", "/api/v1/intake/webhook"
                ));
    }
}
