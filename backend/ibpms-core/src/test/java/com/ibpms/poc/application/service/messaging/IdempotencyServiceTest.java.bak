package com.ibpms.poc.application.service.messaging;

import com.ibpms.poc.infrastructure.jpa.entity.ProcessedMessageEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @Traceability(US = "US-034", CA = "CA-05")
 * TDD: Validación de la lógica de idempotencia para Workers consumidores RabbitMQ.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private UUID testKey;

    @BeforeEach
    void setUp() {
        testKey = UUID.randomUUID();
    }

    @Test
    void isDuplicate_shouldReturnTrue_whenKeyAlreadyExists() {
        when(processedMessageRepository.findByIdempotencyKey(testKey))
                .thenReturn(Optional.of(new ProcessedMessageEntity(testKey, "test.queue")));

        boolean result = idempotencyService.isDuplicate(testKey);

        assertTrue(result, "Un mensaje previamente procesado debe ser reconocido como duplicado.");
        verify(processedMessageRepository).findByIdempotencyKey(testKey);
    }

    @Test
    void isDuplicate_shouldReturnFalse_whenKeyDoesNotExist() {
        when(processedMessageRepository.findByIdempotencyKey(testKey))
                .thenReturn(Optional.empty());

        boolean result = idempotencyService.isDuplicate(testKey);

        assertFalse(result, "Un mensaje nuevo no debe ser reconocido como duplicado.");
    }

    @Test
    void isDuplicate_shouldReturnTrue_whenKeyIsNull() {
        boolean result = idempotencyService.isDuplicate(null);

        assertTrue(result, "Una clave nula debe rechazarse como duplicado por seguridad.");
        verifyNoInteractions(processedMessageRepository);
    }

    @Test
    void registerProcessed_shouldSaveAndReturnTrue_whenNewMessage() {
        when(processedMessageRepository.findByIdempotencyKey(testKey))
                .thenReturn(Optional.empty());
        when(processedMessageRepository.save(any(ProcessedMessageEntity.class)))
                .thenReturn(new ProcessedMessageEntity(testKey, "test.queue"));

        boolean result = idempotencyService.registerProcessed(testKey, "test.queue");

        assertTrue(result, "Un mensaje nuevo debe registrarse correctamente.");
        verify(processedMessageRepository).save(any(ProcessedMessageEntity.class));
    }

    @Test
    void registerProcessed_shouldReturnFalse_whenDuplicate() {
        when(processedMessageRepository.findByIdempotencyKey(testKey))
                .thenReturn(Optional.of(new ProcessedMessageEntity(testKey, "test.queue")));

        boolean result = idempotencyService.registerProcessed(testKey, "test.queue");

        assertFalse(result, "Un mensaje duplicado debe retornar false (ACK silencioso).");
        verify(processedMessageRepository, never()).save(any());
    }

    @Test
    void registerProcessed_shouldReturnFalse_onConcurrentConstraintViolation() {
        when(processedMessageRepository.findByIdempotencyKey(testKey))
                .thenReturn(Optional.empty());
        when(processedMessageRepository.save(any(ProcessedMessageEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        boolean result = idempotencyService.registerProcessed(testKey, "test.queue");

        assertFalse(result, "Una colisión concurrente debe retornar false (Fail-Safe).");
    }
}
