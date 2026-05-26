package com.ibpms.poc.infrastructure.adapters.inbound.messaging;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.infrastructure.mq.config.RabbitMqTopologyConfig;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.ibpms.poc.crosscutting.annotations.Traceability;

@Component
@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})
public class WebhookIntakeConsumer {

    private final WebhookIntakeService intakeService;

    public WebhookIntakeConsumer(WebhookIntakeService intakeService) {
        this.intakeService = intakeService;
    }

    @RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)
    public void processWebhookPayload(WebhookIntakeService.WebhookPayload payload) {
        try {
            intakeService.processIncomingWebhook(payload);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("Pipeline crashed, sending to DLQ ibpms.dlq.global", e);
        }
    }
}
