package com.ibpms.poc.application.service.messaging;

import com.ibpms.poc.infrastructure.jpa.entity.ProcessedMessageEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de Idempotencia para Workers/Consumidores RabbitMQ (CA-05).
 * <p>
 * Valida el header {@code x-idempotency-key} contra la tabla
 * {@code ibpms_processed_messages}. Si el mensaje ya fue procesado,
 * retorna {@code true} para que el consumidor emita un ACK silencioso
 * sin re-ejecutar la lógica de negocio.
 * <p>
 * @Traceability(US = "US-034", CA = "CA-05")
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final ProcessedMessageRepository processedMessageRepository;

    public IdempotencyService(ProcessedMessageRepository processedMessageRepository) {
        this.processedMessageRepository = processedMessageRepository;
    }

    /**
     * Verifica si un mensaje con la clave de idempotencia dada ya fue procesado.
     *
     * @param idempotencyKey UUID único del mensaje.
     * @return {@code true} si ya existe (duplicado), {@code false} si es nuevo.
     */
    @Transactional(readOnly = true)
    public boolean isDuplicate(UUID idempotencyKey) {
        if (idempotencyKey == null) {
            log.warn("[IDEMPOTENCY] Clave de idempotencia nula. Se rechaza por seguridad.");
            return true;
        }
        return processedMessageRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }

    /**
     * Registra un mensaje como procesado exitosamente.
     * Si la inserción falla por constraint UNIQUE, se absorbe el error
     * y se retorna {@code false} (el mensaje ya fue registrado concurrentemente).
     *
     * @param idempotencyKey UUID único del mensaje.
     * @param queueName      Nombre de la cola de origen.
     * @return {@code true} si se registró correctamente; {@code false} si ya existía.
     */
    @Transactional
    public boolean registerProcessed(UUID idempotencyKey, String queueName) {
        if (isDuplicate(idempotencyKey)) {
            log.info("[IDEMPOTENCY] Mensaje duplicado detectado. Key={}, Queue={}. ACK silencioso.", idempotencyKey, queueName);
            return false;
        }

        try {
            ProcessedMessageEntity entity = new ProcessedMessageEntity(idempotencyKey, queueName);
            processedMessageRepository.save(entity);
            log.debug("[IDEMPOTENCY] Mensaje registrado. Key={}, Queue={}", idempotencyKey, queueName);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("[IDEMPOTENCY] Colisión concurrente detectada para Key={}. ACK silencioso.", idempotencyKey);
            return false;
        }
    }
}
