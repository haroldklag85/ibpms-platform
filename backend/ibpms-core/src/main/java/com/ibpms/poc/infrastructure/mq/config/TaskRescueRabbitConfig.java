package com.ibpms.poc.infrastructure.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Infraestructura RabbitMQ para la Gobernanza Asíncrona de Tareas.
 * 
 * <p><strong>Guía Arquitectónica para QA (Routing Keys y DLQ):</strong></p>
 * <ul>
 *   <li><b>EXCHANGE_NAME (ibpms.task.exchange):</b> Punto de entrada central (DirectExchange) para enrutamiento estático de eventos de tareas.</li>
 *   <li><b>QUEUE_NAME (ibpms.task.rescue.queue):</b> Cola durable diseñada para persistir mensajes en caso de caída del Broker. A futuro, actuará en conjunto con una Dead Letter Queue (DLQ) para persistir mensajes envenenados tras fallar los reintentos.</li>
 *   <li><b>ROUTING_KEY (task.unclaim):</b> Clave de enrutamiento específica para la liberación (unclaim) asíncrona de tareas. Los consumidores deben suscribirse a esta directiva para reaccionar al desacople de un Owner sobre el recurso.</li>
 * </ul>
 * 
 * Implementa bases para Dead-Letter-Queues internamente y resiliencia de casos.
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
