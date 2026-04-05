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

    @Bean
    public TopicExchange primaryExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue dlqGlobalQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 2592000000L); // 30 días
        return new Queue(DLQ_GLOBAL, true, false, false, args);
    }

    @Bean
    public Binding dlqBinding() {
        // Enlaza todo lo que caiga al DLX directo a la cola DLQ. Usamos "dlq.route" o similar genérico.
        return BindingBuilder.bind(dlqGlobalQueue()).to(dlxExchange()).with("dlq.route");
    }

    private Map<String, Object> dlxArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.route");
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
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue()).to(primaryExchange()).with("notifications.#");
    }

    @Bean
    public Binding aiGenerationBinding() {
        return BindingBuilder.bind(aiGenerationQueue()).to(primaryExchange()).with("ai.#");
    }

    @Bean
    public Binding integrationsBinding() {
        return BindingBuilder.bind(integrationsWebhookQueue()).to(primaryExchange()).with("integrations.#");
    }

    @Bean
    public Binding bpmnEventsBinding() {
        return BindingBuilder.bind(bpmnEventsQueue()).to(primaryExchange()).with("bpmn.#");
    }

    @Bean
    public Binding taskRescueBinding() {
        return BindingBuilder.bind(taskRescueNewQueue()).to(primaryExchange()).with("task.#");
    }
}
