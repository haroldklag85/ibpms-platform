package com.ibpms.poc.infrastructure.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GhostDeletionSTOMPTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private DelegateTask delegateTask;

    private WorkdeskTaskListener taskListener;

    @BeforeEach
    void setUp() {
        taskListener = new WorkdeskTaskListener(messagingTemplate);
    }

    @Test
    void notify_GivenAssignmentEvent_ShouldPublishStompMessage() {
        // Arrange
        when(delegateTask.getEventName()).thenReturn(TaskListener.EVENTNAME_ASSIGNMENT);
        when(delegateTask.getId()).thenReturn("task-123");
        when(delegateTask.getAssignee()).thenReturn("userA");

        // Act
        taskListener.notify(delegateTask);

        // Assert
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/workdesk/ghost-deletes"), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload).containsEntry("taskId", "task-123");
        assertThat(payload).containsEntry("assignee", "userA");
        assertThat(payload).containsEntry("status", "CLAIMED");
    }

    @Test
    void notify_GivenNonAssignmentEvent_ShouldIgnore() {
        // Arrange
        when(delegateTask.getEventName()).thenReturn(TaskListener.EVENTNAME_COMPLETE);

        // Act
        taskListener.notify(delegateTask);

        // Assert
        verifyNoInteractions(messagingTemplate);
    }
}
