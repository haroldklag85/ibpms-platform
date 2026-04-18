package com.ibpms.poc.infrastructure.mq.health;

import com.ibpms.poc.infrastructure.jpa.entity.QueueFallbackEntity;
import com.ibpms.poc.infrastructure.jpa.repository.QueueFallbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "null"}) // Mockito stubs for generic RabbitTemplate.execute(ChannelCallback<T>)
class RabbitHealthIndicatorTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private QueueFallbackRepository fallbackRepository;

    @InjectMocks
    private RabbitHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        // Reset state si fuera necesario (nuevo mock por cada test provisto por InjectMocks)
    }

    @Test
    void health_ShouldReturnUpIfRabbitMqIsRunning() {
        // Mock the execute call to succeed
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenReturn(true);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("buffer_size", 0);
        assertThat(healthIndicator.isCircuitOpen()).isFalse();
    }

    @Test
    void health_ShouldReturnDownAndOpenCircuitAfterThreeFailures() {
        // Mock the execute call to fail
        when(rabbitTemplate.execute(any(ChannelCallback.class)))
                .thenThrow(new AmqpException("Connection Refused"));

        Health health1 = healthIndicator.health();
        assertThat(health1.getStatus()).isEqualTo(Status.DOWN);
        assertThat(healthIndicator.isCircuitOpen()).isFalse();

        Health health2 = healthIndicator.health();
        assertThat(health2.getStatus()).isEqualTo(Status.DOWN);

        Health health3 = healthIndicator.health();
        assertThat(health3.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health3.getDetails()).containsEntry("status", "CIRCUIT_OPEN");
        assertThat(healthIndicator.isCircuitOpen()).isTrue();
    }

    @Test
    void bufferMessage_ShouldStoreInMemoryIfBufferNotFull() {
        healthIndicator.bufferMessage("test.exchange", "test.routing", "payload");
        
        // El size será 1
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenReturn(true);
        Health health = healthIndicator.health();
        
        assertThat(health.getDetails()).containsEntry("buffer_size", 1);
        verify(fallbackRepository, never()).save(any(QueueFallbackEntity.class));
    }

    @Test
    void bufferMessage_ShouldPersistToDatabaseIfBufferIsFull() {
        // LLenar el buffer hasta 1000
        for (int i = 0; i < 1000; i++) {
            healthIndicator.bufferMessage("test.exchange", "test.routing", "payload-" + i);
        }
        
        // Agregar uno más, debe ir a persistencia
        healthIndicator.bufferMessage("test.exchange", "test.routing", "overflow");

        verify(fallbackRepository, times(1)).save(any(QueueFallbackEntity.class));
    }

    @Test
    void health_ShouldDrainBufferWhenCircuitRecovers() {
        // Falla 3 veces para abrir el circuito
        when(rabbitTemplate.execute(any(ChannelCallback.class)))
                .thenThrow(new AmqpException("Connection Refused"));
        healthIndicator.health();
        healthIndicator.health();
        healthIndicator.health();
        assertThat(healthIndicator.isCircuitOpen()).isTrue();

        // Encolamos un mensaje
        healthIndicator.bufferMessage("test.exchange", "test.routing", "payload-recovered");

        // Falla a UP
        reset(rabbitTemplate); // reset mocks to clear threw exception
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenReturn(true);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(healthIndicator.isCircuitOpen()).isFalse();
        
        // Debe haber invocado convertAndSend para vaciar la cola
        verify(rabbitTemplate, times(1)).convertAndSend("test.exchange", "test.routing", "payload-recovered");
    }
}
