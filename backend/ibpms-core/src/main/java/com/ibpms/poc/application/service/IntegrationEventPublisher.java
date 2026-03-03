package com.ibpms.poc.application.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IntegrationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Cola configurada en infraestructura para el hub
    private static final String INTEGRATION_EXCHANGE = "ibpms.integration.outbound.exchange";

    public IntegrationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * CA-23 Throttling: Encola un evento de integración masivo asíncronamente
     * hacia un Message Broker (RabbitMQ), desacoplando el hilo de ejecución
     * principal.
     */
    public void publishIntegrationEvent(UUID instanceId, String payload, String targetUrl, String pgpPublicKey) {
        OutboundMessage msg = new OutboundMessage(instanceId.toString(), targetUrl, payload, pgpPublicKey);

        // El APIM y los Consumers escucharán esta cola y aplicarán
        // restricciones de rate-limiting (throttling) sin saturar al iBPMS.
        rabbitTemplate.convertAndSend(INTEGRATION_EXCHANGE, "webhook.event", msg);
    }

    // DTO Wrapper para la cola
    public static class OutboundMessage {
        public String instanceId;
        public String targetUrl;
        public String rawPayload;
        public String pgpPublicKey;

        public OutboundMessage(String instanceId, String targetUrl, String rawPayload, String pgpPublicKey) {
            this.instanceId = instanceId;
            this.targetUrl = targetUrl;
            this.rawPayload = rawPayload;
            this.pgpPublicKey = pgpPublicKey;
        }
    }
}
