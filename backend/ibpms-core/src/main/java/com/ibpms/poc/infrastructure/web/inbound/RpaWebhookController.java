package com.ibpms.poc.infrastructure.web.inbound;

import com.ibpms.poc.infrastructure.web.inbound.dto.RpaNotificationDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador de Entrada (Driving Adapter) - Arquitectura Hexagonal.
 * Expone la API REST que los Robots (RPA Scripts) invocarán de forma asíncrona.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inbound")
public class RpaWebhookController {

    /**
     * Endpoint Webhook.
     * Recibe la carga útil del Robot, valida su anatomía, y desencadena el
     * flujo de trabajo asíncrono (Ej. Iniciar Proceso CMMN o Enviar a Kafka).
     * 
     * @param payload Contrato validado por Jakarta Validation.
     * @return 202 Accepted si el payload es correcto, liberando al Robot
     *         inmediatamente.
     */
    @PostMapping("/rpa-webhook")
    public ResponseEntity<Map<String, Object>> receiveRpaNotification(
            @Valid @RequestBody RpaNotificationDTO payload) {

        log.info("🔔 [RPA-WEBHOOK] Alerta judicial recibida desde orígen {}. Radicado: {}",
                payload.getOrigen(), payload.getTramiteId());

        try {
            // TODO: (Sprint 7) Orquestación.
            // 1. Invocar al Caso de Uso "ProcesarNotificacionExternaUseCase".
            // 2. Este caso de uso guardará la metadata en BD y levantará un Evento en
            // Camunda (MessageEvent).
            // 3. En la fase V2, esto publicará el DTO en un tópico de Kafka para
            // procesamiento Stream.

            // Respuesta inmediata para que el Script Python muera (Contenedor Efímero).
            String tracingId = UUID.randomUUID().toString();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ACCEPTED");
            response.put("tracingId", tracingId);
            response.put("message", "Notificación judicial recibida. Evento acoplado al orquestador.");

            log.debug("Procesamiento rápido de Webhook RPA. Tracing ID: {}", tracingId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            log.error("❌ Error grave procesando payload del RPA: {}", e.getMessage(), e);
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("status", "ERROR");
            errorRes.put("message", "Fallo interno en orquestador al consumir webhook.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorRes);
        }
    }
}
