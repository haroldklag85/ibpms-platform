package com.ibpms.poc.infrastructure.messaging;

import com.ibpms.poc.application.service.cache.AiDmnCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * GAP-14: Listener Asíncrono para invalidar caché de DMNs
 * cuando cambia un Form Schema.
 */
// @Traceability: US-007, CA-16
@Component
public class FormSchemaChangedRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(FormSchemaChangedRabbitListener.class);
    private final AiDmnCacheService cacheService;

    public FormSchemaChangedRabbitListener(AiDmnCacheService cacheService) {
        this.cacheService = cacheService;
    }

    // TODO: El publicador de FORM_SCHEMA_CHANGED en US-003 no existe aún. Debe crearse en la historia correspondiente.
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ibpms.dmn.schema.changed.queue", durable = "true"),
            exchange = @Exchange(value = "ibpms.events.exchange", type = "topic"),
            key = "form.schema.changed"
    ))
    public void onFormSchemaChanged(String payload) {
        log.info("[RABBITMQ] Evento FORM_SCHEMA_CHANGED recibido. Purgando caché DMN asociada. Payload: {}", payload);
        
        try {
            // Lógica de purga selectiva (o total en V1)
            // Se asume un invalidate total de la caché DMN por seguridad en V1.
            cacheService.evictAll();
            log.info("[RABBITMQ] Purga de caché DMN completada.");
        } catch (Exception e) {
            log.error("[RABBITMQ] Error purgando caché DMN.", e);
            throw e; // Nack y re-queue (u orientar a DLQ)
        }
    }
}
