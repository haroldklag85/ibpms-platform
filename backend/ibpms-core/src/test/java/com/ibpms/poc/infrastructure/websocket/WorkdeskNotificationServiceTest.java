package com.ibpms.poc.infrastructure.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WorkdeskNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WorkdeskNotificationService notificationService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyTaskClaimedWithCorrectPayloadAndDestination() {
        // Arrange
        String tenantId = "tenantX";
        String taskId = "task-123";
        String claimedBy = "user-abc";

        // Act
        notificationService.notifyTaskClaimed(tenantId, taskId, claimedBy);

        // Assert
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/workdesk/tenantX"),
            payloadCaptor.capture()
        );

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload).containsEntry("type", "TASK_CLAIMED");
        assertThat(payload).containsEntry("taskId", "task-123");
        assertThat(payload).containsEntry("claimedBy", "user-abc");
        assertThat(payload).containsKey("timestamp");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyTaskUnclaimedWithCorrectPayloadAndDestination() {
        // Arrange
        String tenantId = "tenantY";
        String taskId = "task-456";

        // Act
        notificationService.notifyTaskUnclaimed(tenantId, taskId);

        // Assert
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/workdesk/tenantY"),
            payloadCaptor.capture()
        );

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload).containsEntry("type", "TASK_UNCLAIMED");
        assertThat(payload).containsEntry("taskId", "task-456");
        assertThat(payload).containsKey("timestamp");
    }
}
