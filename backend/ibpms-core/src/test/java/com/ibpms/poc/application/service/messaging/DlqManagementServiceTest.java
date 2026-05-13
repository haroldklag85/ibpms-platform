package com.ibpms.poc.application.service.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @Traceability(US = "US-034", CA = "CA-02")
 * TDD: Validación del servicio de gestión DLQ.
 */
@ExtendWith(MockitoExtension.class)
class DlqManagementServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private DlqManagementService dlqManagementService;

    @Test
    void getDlqSummary_shouldReturnZeroMessages_whenQueueIsEmpty() {
        Properties props = new Properties();
        props.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, "0");
        when(rabbitAdmin.getQueueProperties("ibpms.dlq.global")).thenReturn(props);

        Map<String, Object> summary = dlqManagementService.getDlqSummary();

        assertEquals(0, summary.get("totalMessages"));
        assertNotNull(summary.get("oldestMessages"));
    }

    @Test
    void getDlqSummary_shouldReturnMessageCount_whenQueueHasMessages() {
        Properties props = new Properties();
        props.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, "5");
        when(rabbitAdmin.getQueueProperties("ibpms.dlq.global")).thenReturn(props);

        MessageProperties msgProps = new MessageProperties();
        msgProps.setMessageId("msg-1");
        Message mockMsg = new Message("{\"event\":\"test\"}".getBytes(), msgProps);
        when(rabbitTemplate.receive(eq("ibpms.dlq.global"), anyLong()))
                .thenReturn(mockMsg)
                .thenReturn(null);

        Map<String, Object> summary = dlqManagementService.getDlqSummary();

        assertEquals(5, summary.get("totalMessages"));
    }

    @Test
    void retryMessages_shouldReturnZero_whenQueueIsEmpty() {
        when(rabbitTemplate.receive(eq("ibpms.dlq.global"), anyLong())).thenReturn(null);

        int retried = dlqManagementService.retryMessages(10);

        assertEquals(0, retried);
    }

    @Test
    void retryMessages_shouldRequeueMessages_toOriginalExchange() {
        MessageProperties msgProps = new MessageProperties();
        msgProps.setReceivedRoutingKey("business.high.process");
        Message mockMsg = new Message("{\"data\":1}".getBytes(), msgProps);
        when(rabbitTemplate.receive(eq("ibpms.dlq.global"), anyLong()))
                .thenReturn(mockMsg)
                .thenReturn(null);

        int retried = dlqManagementService.retryMessages(10);

        assertEquals(1, retried);
        verify(rabbitTemplate).send(anyString(), anyString(), eq(mockMsg));
    }

    @Test
    void purge_shouldInvokePurgeQueue() {
        dlqManagementService.purge();

        verify(rabbitAdmin).purgeQueue("ibpms.dlq.global");
    }
}
