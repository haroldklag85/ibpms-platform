package com.ibpms.poc.infrastructure.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqTopologyConfig {

    public static final String TOPIC_EXCHANGE = "ibpms.exchange.topic";
    public static final String DLX_EXCHANGE = "ibpms.exchange.dlx";
    public static final String DLQ_GLOBAL = "ibpms.dlq.global";

    public static final String QUEUE_NOTIFICATIONS_EMAIL = "ibpms.notifications.email";
    public static final String QUEUE_AI_GENERATION = "ibpms.ai.generation";
    public static final String QUEUE_INTEGRATIONS_WEBHOOK = "ibpms.integrations.webhook";
    public static final String QUEUE_BPMN_EVENTS = "ibpms.bpmn.events";
    public static final String QUEUE_TASK_RESCUE = "ibpms.task.rescue";

    // @Traceability: Remediación Colisión Beans RabbitMQ J-02 (T-24)
    // Exchanges y DLQ manejados por RabbitMQConfig.java

    private Map<String, Object> dlxArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.global");
        args.put("x-max-priority", 10);
        return args;
    }

    // Colas con DLX integrado
    @Bean
    public Queue notificationsQueue() {
        return new Queue(QUEUE_NOTIFICATIONS_EMAIL, true, false, false, dlxArgs());
    }

    @Bean
    public Queue aiGenerationQueue() {
        return new Queue(QUEUE_AI_GENERATION, true, false, false, dlxArgs());
    }

    @Bean
    public Queue integrationsWebhookQueue() {
        return new Queue(QUEUE_INTEGRATIONS_WEBHOOK, true, false, false, dlxArgs());
    }

    @Bean
    public Queue bpmnEventsQueue() {
        return new Queue(QUEUE_BPMN_EVENTS, true, false, false, dlxArgs());
    }

    @Bean
    public Queue taskRescueNewQueue() {
        return new Queue(QUEUE_TASK_RESCUE, true, false, false, dlxArgs());
    }

    // Bindings
    @Bean
    public Binding notificationsBinding(TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(notificationsQueue()).to(ibpmsTopicExchange).with("notifications.#");
    }

    @Bean
    public Binding aiGenerationBinding(TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(aiGenerationQueue()).to(ibpmsTopicExchange).with("ai.#");
    }

    @Bean
    public Binding integrationsBinding(TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(integrationsWebhookQueue()).to(ibpmsTopicExchange).with("integrations.#");
    }

    @Bean
    public Binding bpmnEventsBinding(TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(bpmnEventsQueue()).to(ibpmsTopicExchange).with("bpmn.#");
    }

    @Bean
    public Binding taskRescueBinding(TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(taskRescueNewQueue()).to(ibpmsTopicExchange).with("task.#");
    }
}
