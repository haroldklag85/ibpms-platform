package com.ibpms.poc.infrastructure.websocket;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WorkdeskNotificationServiceTest {

    @Test
    public void testNotifyTaskClaimed() {
        SimpMessagingTemplate templateMock = mock(SimpMessagingTemplate.class);
        WorkdeskNotificationService service = new WorkdeskNotificationService(templateMock);

        service.notifyTaskClaimed("tenant-alpha", "task-99", "admin.user");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(templateMock).convertAndSend(eq("/topic/workdesk/tenant-alpha"), payloadCaptor.capture());

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload.get("type")).isEqualTo("TASK_CLAIMED");
        assertThat(capturedPayload.get("taskId")).isEqualTo("task-99");
        assertThat(capturedPayload.get("claimedBy")).isEqualTo("admin.user");
        assertThat(capturedPayload.containsKey("timestamp")).isTrue();
    }
}
