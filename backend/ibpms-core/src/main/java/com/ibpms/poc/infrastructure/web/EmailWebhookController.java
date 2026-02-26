package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.ProcesarEmailWebhookUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adaptador Driving — Controlador REST HTTP para Graph API o Azure Logic Apps.
 */
@RestController
@RequestMapping("/inbound/email-webhook")
public class EmailWebhookController {

    private final ProcesarEmailWebhookUseCase webhookUseCase;

    @Value("${app.webhook.ms-graph.client-state:secreto-compartido-m365}")
    private String expectedClientState;

    public EmailWebhookController(ProcesarEmailWebhookUseCase webhookUseCase) {
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> receiveEmailNotification(
            @RequestHeader(value = "ClientState", required = false) String clientState,
            @RequestBody Map<String, Object> payload) {

        // 1. Verificación de Origen (Auth por ClientState, no por JWT)
        if (clientState == null || !clientState.equals(expectedClientState)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 No Autorizado
        }

        // 2. Extracción defensiva del Payload Mock (MS Graph Resource Data)
        String subject = (String) payload.getOrDefault("subject", "Asunto no definido");
        String body = (String) payload.getOrDefault("body", "");
        String sender = (String) payload.getOrDefault("sender", "unknown@domain.com");

        // 3. Orquestación Asíncrona (A fines de PoC es sincrónica con 202)
        webhookUseCase.procesarEmail(subject, body, sender);

        // 4. Retorno HTTP 202 Accepted (Standard de Webhooks para evitar time-outs)
        return ResponseEntity.accepted().build();
    }
}
