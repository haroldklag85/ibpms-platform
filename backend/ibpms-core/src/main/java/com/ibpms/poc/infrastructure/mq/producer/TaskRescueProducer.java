package com.ibpms.poc.infrastructure.mq.producer;

import com.ibpms.poc.infrastructure.mq.config.TaskRescueRabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CA-07 Productor AMQP. Dispara mensajes de rescate (Unclaim) hacia el Bus de Eventos.
 */
@Service
public class TaskRescueProducer {

    private static final Logger log = LoggerFactory.getLogger(TaskRescueProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public TaskRescueProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void triggerMassiveUnclaim(String userId) {
        log.info("[RABBIT-MQ] Publicando evento asíncrono MASSIVE_UNCLAIM para el usuario: {}", userId);
        
        // Payload mínimo
        Map<String, String> payload = Map.of(
                "action", "UNCLAIM_ALL",
                "userId", userId
        );
        
        rabbitTemplate.convertAndSend(
                TaskRescueRabbitConfig.EXCHANGE_NAME,
                TaskRescueRabbitConfig.ROUTING_KEY,
                payload
        );
    }
}
