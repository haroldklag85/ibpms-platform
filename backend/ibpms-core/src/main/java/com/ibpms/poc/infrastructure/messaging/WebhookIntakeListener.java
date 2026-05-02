package com.ibpms.poc.infrastructure.messaging;

import com.ibpms.poc.application.service.WebhookIntakeService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WebhookIntakeListener {

    private final WebhookIntakeService intakeService;

    public WebhookIntakeListener(WebhookIntakeService intakeService) {
        this.intakeService = intakeService;
    }

    @RabbitListener(queues = "ibpms.integrations.webhook")
    public void processWebhookPayload(WebhookIntakeService.WebhookPayload payload) {
        try {
            intakeService.processIncomingWebhook(payload);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("Pipeline crashed, sending to DLQ ibpms.dlq.global", e);
        }
    }
}
