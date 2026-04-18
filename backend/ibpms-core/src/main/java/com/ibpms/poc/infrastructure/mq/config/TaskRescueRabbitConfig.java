package com.ibpms.poc.infrastructure.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Infraestructura RabbitMQ para la Gobernanza Asíncrona de Tareas.
 * Integrado a la nueva topología CA-4 con Dead Letter Exchange.
 */
@Configuration
public class TaskRescueRabbitConfig {

    public static final String EXCHANGE_NAME = "ibpms.task.exchange";
    // Nota: Mantenemos el nombre original para no romper código Legacy, 
    // pero redirigido a la nueva norma via config de DLX.
    public static final String QUEUE_NAME = "ibpms.task.rescue.queue";
    public static final String ROUTING_KEY = "task.unclaim";

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue taskRescueQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMqTopologyConfig.DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.route");
        // Cola durable para sobrevivir a reinicios del Broker, integrada al DLQ.
        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Binding bindingTaskRescue(Queue taskRescueQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskRescueQueue).to(taskExchange).with(ROUTING_KEY);
    }
}
