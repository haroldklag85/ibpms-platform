package com.ibpms.poc.infrastructure.mq.producer;

import com.ibpms.poc.infrastructure.jpa.entity.QueueFallbackEntity;
import com.ibpms.poc.infrastructure.jpa.repository.QueueFallbackRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitProducerComponent {

    private final RabbitTemplate rabbitTemplate;
    private final QueueFallbackRepository fallbackRepository;

    @CircuitBreaker(name = "rabbitCluster", fallbackMethod = "fallbackToSql")
    public void convertAndSend(String exchange, String routingKey, Object message) {
        log.debug("Enviando mensaje AMQP a exchange {} routingKey {}", exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @Transactional
    public void fallbackToSql(String exchange, String routingKey, Object message, Throwable t) {
        log.error("💥 Circuit Breaker activado (AMQP Caído/Saturado) - CA-10. Persistiendo mensaje transaccionalmente en Fallback. Exception: {}", t.getMessage());
        
        QueueFallbackEntity entity = new QueueFallbackEntity();
        entity.setMessageBody(message != null ? message.toString() : "");
        entity.setTargetQueue(routingKey);
        entity.setHeadersJson("{\"exchange\": \"" + exchange + "\"}");
        
        fallbackRepository.save(entity);
        log.info("Mensaje salvado exitosamente en tabla ibpms_queue_fallback con ID {}", entity.getId());
    }
}
