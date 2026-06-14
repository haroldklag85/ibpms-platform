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

    public void triggerDelegationUnclaim(String userId) {
        log.info("[RABBIT-MQ] Publicando evento asíncrono DELEGATION_UNCLAIM para el usuario: {}", userId);
        
        Map<String, String> payload = Map.of(
                "action", "UNCLAIM_ALL_DELEGATION",
                "userId", userId
        );
        
        rabbitTemplate.convertAndSend(
                "ibpms.security.exchange",
                "security.user.delegated",
                payload
        );
    }

    public void triggerDeactivationUnclaim(String userId) {
        log.info("[RABBIT-MQ] Publicando evento asíncrono DEACTIVATION_UNCLAIM para el usuario: {}", userId);
        
        Map<String, String> payload = Map.of(
                "action", "UNCLAIM_ALL_DEACTIVATION",
                "userId", userId
        );
        
        rabbitTemplate.convertAndSend(
                "ibpms.security.exchange",
                "security.user.deactivated",
                payload
        );
    }
}
