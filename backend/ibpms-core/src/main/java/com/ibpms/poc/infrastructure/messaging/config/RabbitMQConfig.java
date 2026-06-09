package com.ibpms.poc.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de la topología RabbitMQ para iBPMS.
 * <p>
 * @Traceability(US = "US-034", CA = {"CA-01", "CA-03", "CA-04"})
 * <ul>
 *   <li>CA-01: Broker Exclusivo de Alta Demanda — Exchange Topic principal.</li>
 *   <li>CA-03: Priority Queues — Colas con x-max-priority: 10.</li>
 *   <li>CA-04: Catálogo Oficial — Exchange, DLX, DLQ, Routing Keys.</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    // --- Exchange Names ---
    public static final String TOPIC_EXCHANGE = "ibpms.exchange.topic";
    public static final String DLX_EXCHANGE = "ibpms.exchange.dlx";
    public static final String SECURITY_EXCHANGE = "ibpms.security.exchange";

    // --- Queue Names ---
    public static final String DLQ_GLOBAL = "ibpms.dlq.global";
    public static final String BUSINESS_QUEUE = "ibpms.queue.business";
    public static final String UNCLAIM_QUEUE = "camunda.task.unclaim.queue";

    // --- Routing Keys ---
    public static final String DLQ_ROUTING_KEY = "dlq.global";
    public static final String DELEGATED_ROUTING_KEY = "security.user.delegated";
    public static final String DEACTIVATED_ROUTING_KEY = "security.user.deactivated";

    // === Exchanges ===

    @Bean
    public TopicExchange ibpmsTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange ibpmsDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange ibpmsSecurityExchange() {
        return new TopicExchange(SECURITY_EXCHANGE, true, false);
    }

    // === Queues ===

    /**
     * Cola Global de Dead Letters.
     * Recibe mensajes rechazados o expirados de cualquier cola del ecosistema.
     */
    @Bean
    public Queue ibpmsDlqGlobal() {
        return QueueBuilder.durable(DLQ_GLOBAL)
                .withArgument("x-message-ttl", 2592000000L) // 30 días — Congruente con RabbitMqTopologyConfig
                .build();
    }

    /**
     * Cola de negocio base con soporte de prioridad (CA-03).
     * Vinculada al DLX para redirigir mensajes fallidos a la DLQ global.
     */
    @Bean
    public Queue ibpmsBusinessQueue() {
        return QueueBuilder.durable(BUSINESS_QUEUE)
                .withArgument("x-max-priority", 10)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue camundaTaskUnclaimQueue() {
        return QueueBuilder.durable(UNCLAIM_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    // === Bindings ===

    @Bean
    public Binding dlqBinding(Queue ibpmsDlqGlobal, DirectExchange ibpmsDlxExchange) {
        return BindingBuilder.bind(ibpmsDlqGlobal).to(ibpmsDlxExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding businessQueueBinding(Queue ibpmsBusinessQueue, TopicExchange ibpmsTopicExchange) {
        return BindingBuilder.bind(ibpmsBusinessQueue).to(ibpmsTopicExchange).with("business.#");
    }

    @Bean
    public Binding unclaimDelegatedBinding(Queue camundaTaskUnclaimQueue, TopicExchange ibpmsSecurityExchange) {
        return BindingBuilder.bind(camundaTaskUnclaimQueue).to(ibpmsSecurityExchange).with(DELEGATED_ROUTING_KEY);
    }

    @Bean
    public Binding unclaimDeactivatedBinding(Queue camundaTaskUnclaimQueue, TopicExchange ibpmsSecurityExchange) {
        return BindingBuilder.bind(camundaTaskUnclaimQueue).to(ibpmsSecurityExchange).with(DEACTIVATED_ROUTING_KEY);
    }

    // === Message Converter ===

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
