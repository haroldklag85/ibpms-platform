package com.ibpms.poc.infrastructure.mq.health;

import com.ibpms.poc.infrastructure.jpa.entity.QueueFallbackEntity;
import com.ibpms.poc.infrastructure.jpa.repository.QueueFallbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RabbitHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(RabbitHealthIndicator.class);

    private final RabbitTemplate rabbitTemplate;
    private final QueueFallbackRepository fallbackRepository;

    // Buffer temporal nativo (limitado a 1000)
    private final Queue<FallbackMessage> memoryBuffer = new ConcurrentLinkedQueue<>();
    private int failureCount = 0;
    private boolean circuitOpen = false;

    public RabbitHealthIndicator(RabbitTemplate rabbitTemplate, QueueFallbackRepository fallbackRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.fallbackRepository = fallbackRepository;
    }

    @Override
    public Health health() {
        try {
            // Ping genérico o revisión de estado a un exchange. Una operacion segura es chequear info del connection factory.
            rabbitTemplate.execute(channel -> {
                channel.isOpen();
                return true;
            });
            
            if (circuitOpen) {
                log.info("RabbitMQ Health restablecido. Drenando buffer...");
                circuitOpen = false;
                failureCount = 0;
                drainBuffer();
            }
            
            return Health.up().withDetail("buffer_size", memoryBuffer.size()).build();
        } catch (AmqpException e) {
            failureCount++;
            if (failureCount >= 3) {
                circuitOpen = true;
                return Health.down(e).withDetail("status", "CIRCUIT_OPEN").withDetail("buffer_size", memoryBuffer.size()).build();
            }
            return Health.down(e).withDetail("status", "FAILING").withDetail("retries", failureCount).build();
        }
    }

    public boolean isCircuitOpen() {
        return circuitOpen;
    }

    public void bufferMessage(String exchange, String routingKey, String body) {
        if (memoryBuffer.size() < 1000) {
            memoryBuffer.add(new FallbackMessage(exchange, routingKey, body));
        } else {
            // Si el buffer local está lleno, persitir en BD directamente
            QueueFallbackEntity entity = new QueueFallbackEntity();
            entity.setTargetQueue(routingKey);
            entity.setMessageBody(body);
            fallbackRepository.save(entity);
        }
    }

    private void drainBuffer() {
        while (!memoryBuffer.isEmpty()) {
            FallbackMessage msg = memoryBuffer.poll();
            rabbitTemplate.convertAndSend(msg.exchange, msg.routingKey, msg.body);
        }
    }

    private static class FallbackMessage {
        String exchange;
        String routingKey;
        String body;
        FallbackMessage(String exchange, String routingKey, String body) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.body = body;
        }
    }
}
