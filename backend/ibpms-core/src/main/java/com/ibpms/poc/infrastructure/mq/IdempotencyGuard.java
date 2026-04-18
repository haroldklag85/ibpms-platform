package com.ibpms.poc.infrastructure.mq;

import com.ibpms.poc.infrastructure.jpa.entity.ProcessedMessageEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class IdempotencyGuard {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyGuard.class);
    private final ProcessedMessageRepository idempotencyRepository;

    public IdempotencyGuard(ProcessedMessageRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Message amqpMessage = null;
        for (Object arg : args) {
            if (arg instanceof Message) {
                amqpMessage = (Message) arg;
                break;
            }
        }

        if (amqpMessage != null) {
            String idempotencyKey = (String) amqpMessage.getMessageProperties().getHeaders().get("x-idempotency-key");
            String queueName = amqpMessage.getMessageProperties().getConsumerQueue();

            if (idempotencyKey != null) {
                if (idempotencyRepository.existsById(idempotencyKey)) {
                    log.warn("Mensaje duplicado descartado silenciosamente (ACK). IdempotencyKey: {}, Queue: {}", idempotencyKey, queueName);
                    return null; // ACK silencioso
                }
                // Si no existe, permitimos el flujo y luego lo registramos (si no salta exception)
                Object result = joinPoint.proceed();
                idempotencyRepository.save(new ProcessedMessageEntity(idempotencyKey, queueName != null ? queueName : "unknown"));
                return result;
            }
        }
        
        // Si no hay mensaje o no tiene key, continuamos normalmente
        return joinPoint.proceed();
    }
}
