package com.ibpms.poc.infrastructure.web;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Inbound Webhooks Dinámicos (Pantalla 8 - CA-21).
 * Recibe callbacks de sistemas externos y avanza tokens pausados en Camunda vía
 * MessageEvent intermedio.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class InboundWebhookController {

    private static final Logger log = LoggerFactory.getLogger(InboundWebhookController.class);
    private final RuntimeService runtimeService;

    public InboundWebhookController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Endpoint dinámico para recibir payload externo.
     * 
     * @param webhookId   Identificador único correlacionado en el BusinessKey o
     *                    Variable del proceso.
     * @param messageName Nombre del MessageRef en Camunda que el sistema externo
     *                    desea disparar.
     */
    @PostMapping("/{webhookId}")
    public ResponseEntity<String> receiveWebhook(
            @PathVariable String webhookId,
            @RequestParam(required = true) String messageName,
            @RequestBody Map<String, Object> payload) {

        log.info("Recibiendo Webhook ID [{}] apuntando al mensaje [{}]", webhookId, messageName);

        try {
            // Se correlaciona con la instancia que esté esperando el messageName y
            // que tenga una variable "waitingWebhookId" igual al webhookId.
            MessageCorrelationResult result = runtimeService.createMessageCorrelation(messageName)
                    .processInstanceVariableEquals("waitingWebhookId", webhookId)
                    .setVariables(payload) // Inyectamos el body del webhook al proceso de Camunda
                    .correlateWithResult();

            log.info("Correlación exitosa. Instancia avanzada: {}", result.getExecution().getProcessInstanceId());
            return ResponseEntity.ok("Webhook recibido y correlacionado con éxito.");

        } catch (org.camunda.bpm.engine.MismatchingMessageCorrelationException e) {
            log.warn("El Webhook ID [{}] no encontró instancias pausadas esperando el mensaje [{}].", webhookId,
                    messageName);
            // HTTP 404 porque no se halló a quién entregar el mensaje
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Fallo interno procesando webhook dinámico.", e);
            return ResponseEntity.internalServerError().body("Error interno procesando webhook.");
        }
    }
}
