package com.ibpms.poc.infrastructure.mq.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class TaskRescueProducerTest {

    private RabbitTemplate rabbitTemplate;
    private TaskRescueProducer producer;

    @BeforeEach
    void setUp() {
        rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        producer = new TaskRescueProducer(rabbitTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTriggerDelegationUnclaim() {
        String userId = "test-donor";
        producer.triggerDelegationUnclaim(userId);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("ibpms.security.exchange"),
                eq("security.user.delegated"),
                payloadCaptor.capture()
        );

        Map<String, String> payload = (Map<String, String>) payloadCaptor.getValue();
        assertEquals("UNCLAIM_ALL_DELEGATION", payload.get("action"));
        assertEquals(userId, payload.get("userId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTriggerDeactivationUnclaim() {
        String userId = "test-deactivated";
        producer.triggerDeactivationUnclaim(userId);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("ibpms.security.exchange"),
                eq("security.user.deactivated"),
                payloadCaptor.capture()
        );

        Map<String, String> payload = (Map<String, String>) payloadCaptor.getValue();
        assertEquals("UNCLAIM_ALL_DEACTIVATION", payload.get("action"));
        assertEquals(userId, payload.get("userId"));
    }
}
