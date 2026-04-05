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
        amqpMessage.getMessageProperties().setHeader("x-idempotency-key", "abc-123");
        when(joinPoint.getArgs()).thenReturn(new Object[]{amqpMessage});
        when(idempotencyRepository.existsById("abc-123")).thenReturn(false);
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
        amqpMessage.getMessageProperties().setHeader("x-idempotency-key", "dup-456");
        when(joinPoint.getArgs()).thenReturn(new Object[]{amqpMessage});
        when(idempotencyRepository.existsById("dup-456")).thenReturn(true);

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
