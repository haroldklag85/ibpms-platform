package com.ibpms.poc.infrastructure.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Infraestructura RabbitMQ para la Gobernanza Asíncrona de Tareas.
 * Implementa Dead-Letter-Queues internamente y Exorcismo de Casos.
 */
@Configuration
public class TaskRescueRabbitConfig {

    public static final String EXCHANGE_NAME = "ibpms.task.exchange";
    public static final String QUEUE_NAME = "ibpms.task.rescue.queue";
    public static final String ROUTING_KEY = "task.unclaim";

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue taskRescueQueue() {
        // Cola durable para sobrevivir a reinicios del Broker
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding bindingTaskRescue(Queue taskRescueQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskRescueQueue).to(taskExchange).with(ROUTING_KEY);
    }
}
