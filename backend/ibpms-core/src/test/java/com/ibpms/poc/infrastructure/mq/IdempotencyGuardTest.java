package com.ibpms.poc.infrastructure.mq;

import com.ibpms.poc.infrastructure.jpa.entity.ProcessedMessageEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null") // Mockito stubs for @NonNull JPA repository methods
class IdempotencyGuardTest {

    @Mock
    private ProcessedMessageRepository idempotencyRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private IdempotencyGuard idempotencyGuard;

    private Message amqpMessage;

    @BeforeEach
    void setUp() {
        MessageProperties props = new MessageProperties();
        props.setConsumerQueue("test.queue");
        amqpMessage = new Message("body".getBytes(), props);
    }

    @Test
    void shouldProcessNewMessageAndSaveIdempotencyKey() throws Throwable {
        // Arrange
        amqpMessage.getMessageProperties().setHeader("x-idempotency-key", "123e4567-e89b-12d3-a456-426614174000");
        when(joinPoint.getArgs()).thenReturn(new Object[]{amqpMessage});
        when(idempotencyRepository.findByIdempotencyKey(any(java.util.UUID.class))).thenReturn(java.util.Optional.empty());
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        // Act
        Object result = idempotencyGuard.checkIdempotency(joinPoint);

        // Assert
        assertThat(result).isEqualTo("SUCCESS");
        verify(joinPoint).proceed();
        verify(idempotencyRepository).save(any(ProcessedMessageEntity.class));
    }

    @Test
    void shouldDropDuplicateMessageSilently() throws Throwable {
        // Arrange
        amqpMessage.getMessageProperties().setHeader("x-idempotency-key", "123e4567-e89b-12d3-a456-426614174001");
        when(joinPoint.getArgs()).thenReturn(new Object[]{amqpMessage});
        when(idempotencyRepository.findByIdempotencyKey(any(java.util.UUID.class))).thenReturn(java.util.Optional.of(new ProcessedMessageEntity()));

        // Act
        Object result = idempotencyGuard.checkIdempotency(joinPoint);

        // Assert
        assertThat(result).isNull(); // ACK silencioso
        verify(joinPoint, never()).proceed();
        verify(idempotencyRepository, never()).save(any(ProcessedMessageEntity.class));
    }

    @Test
    void shouldProceedNormallyIfNoIdempotencyKeyIsPresent() throws Throwable {
        // Arrange
        // No header set
        when(joinPoint.getArgs()).thenReturn(new Object[]{amqpMessage});
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        // Act
        Object result = idempotencyGuard.checkIdempotency(joinPoint);

        // Assert
        assertThat(result).isEqualTo("SUCCESS");
        verify(joinPoint).proceed();
        verify(idempotencyRepository, never()).save(any(ProcessedMessageEntity.class));
    }
}
