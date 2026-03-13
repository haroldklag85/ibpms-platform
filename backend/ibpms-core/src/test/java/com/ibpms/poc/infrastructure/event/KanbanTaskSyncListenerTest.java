package com.ibpms.poc.infrastructure.event;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class KanbanTaskSyncListenerTest {

    @Mock
    private WorkdeskProjectionRepository projectionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KanbanTaskSyncListener listener;

    private KanbanTaskEntity mockTask;

    @BeforeEach
    void setUp() {
        listener.setProjectionRepository(projectionRepository);
        listener.setMessagingTemplate(messagingTemplate);

        mockTask = new KanbanTaskEntity();
        mockTask.setId(UUID.randomUUID());
        mockTask.setTitle("QA Audit Task");
        mockTask.setStatus("OPEN");
    }

    @Test
    void whenTaskHasNoAssignee_thenSyncsToCQRS_ButDoesNotBroadcastWebsocket() {
        // Arrange
        when(projectionRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        listener.syncToProjection(mockTask);

        // Assert
        verify(projectionRepository, times(1)).save(any(WorkdeskProjectionEntity.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void whenTaskIsAssignedOrCompleted_thenSyncsToCQRS_AndBroadcastsWebsocket() {
        // Arrange
        mockTask.setAssignee("agent.smith");
        when(projectionRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        listener.syncToProjection(mockTask);

        // Assert CQRS Sync
        ArgumentCaptor<WorkdeskProjectionEntity> entityCaptor = ArgumentCaptor.forClass(WorkdeskProjectionEntity.class);
        verify(projectionRepository, times(1)).save(entityCaptor.capture());
        
        WorkdeskProjectionEntity saved = entityCaptor.getValue();
        assertEquals("KANBAN-" + mockTask.getId(), saved.getId());
        assertEquals("agent.smith", saved.getAssignee());

        // Assert Websocket Broadcast
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("/topic/workdesk.updates", topicCaptor.getValue());
        // Assert the specific payload format
        assertTrue(payloadCaptor.getValue().contains("\"event\": \"TASK_CLAIMED\""));
        assertTrue(payloadCaptor.getValue().contains(mockTask.getId().toString()));
    }
}
