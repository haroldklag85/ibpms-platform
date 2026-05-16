package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.in.CreateExpedienteUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import com.ibpms.poc.application.service.InboundWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Enrutador Dinámico de Webhooks (Inbound).
 * Reemplaza los endpoints estáticos (ej. /inbound/email).
 * Actúa como "Facade" para inyectar payloads genéricos externos hacia
 * procesos BPMN específicos configurados en base de datos.
 */
@RestController
@RequestMapping("/api/v1/dynamic-webhook")
@Traceability(US = "US-023", CA = {"CA-01"})
public class DynamicWebhookRouterController {

    private final InboundWebhookService inboundService;
    private final CreateExpedienteUseCase createExpedienteUseCase;

    public DynamicWebhookRouterController(InboundWebhookService inboundService,
            CreateExpedienteUseCase createExpedienteUseCase) {
        this.inboundService = inboundService;
        this.createExpedienteUseCase = createExpedienteUseCase;
    }

    @PostMapping("/{webhookId}")
    public ResponseEntity<?> handleIncomingWebhook(
            @PathVariable UUID webhookId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "ClientState", required = false) String clientState,
            @RequestBody Map<String, Object> payload) {

        // 1. Buscar configuración en Base de Datos
        // @Traceability: Retro-Remediación ADR-001
        Optional<InboundWebhookEntity> webhookOpt = inboundService.findByIdAndIsActiveTrue(webhookId);
        if (webhookOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Webhook ID no encontrado o inactivo.");
        }

        InboundWebhookEntity config = webhookOpt.get();

        // 2. Validación muy rudimentaria de seguridad genérica
        if (config.getSecurityToken() != null && !config.getSecurityToken().isEmpty()) {
            boolean valid = false;
            if (authHeader != null && authHeader.contains(config.getSecurityToken()))
                valid = true;
            if (clientState != null && clientState.contains(config.getSecurityToken()))
                valid = true;

            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma Invalida / Token requerido.");
            }
        }

        // 3. Crear el caso de negocio inyectando el payload como variables XML/JSON
        ExpedienteDTO requestDTO = new ExpedienteDTO();
        requestDTO.setDefinitionKey(config.getTargetBpmnProcessKey());
        requestDTO.setBusinessKey(UUID.randomUUID().toString()); // Podríamos derivarlo del payload
        requestDTO.setType("WEBHOOK_GENERICO");
        requestDTO.setVariables(payload);

        ExpedienteDTO response = createExpedienteUseCase.create(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
