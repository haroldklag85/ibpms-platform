package com.ibpms.poc.infrastructure.event;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class KanbanTaskSyncListenerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KanbanTaskSyncListener listener;

    private KanbanTaskEntity mockTask;

    @BeforeEach
    void setUp() {
        listener.setJdbcTemplate(jdbcTemplate);
        listener.setMessagingTemplate(messagingTemplate);

        mockTask = new KanbanTaskEntity();
        mockTask.setId(UUID.randomUUID());
        mockTask.setTitle("QA Audit Task");
        mockTask.setStatus("OPEN");
    }

    @Test
    void whenTaskHasNoAssignee_thenSyncsToCQRS_AndBroadcastsUpdateWebsocket() {
        // Act
        listener.syncToProjection(mockTask);

        // Assert
        verify(jdbcTemplate, times(1)).update(
            anyString(),
            eq("KANBAN-" + mockTask.getId()),
            eq("KANBAN"),
            eq(mockTask.getId().toString()),
            eq("QA Audit Task"),
            eq(null),
            any(),
            eq("OPEN"),
            eq("default"),
            eq(1)
        );
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/workdesk/default"),
            any(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.class)
        );
    }

    @Test
    void whenTaskIsAssignedOrCompleted_thenSyncsToCQRS_AndBroadcastsWebsocket() {
        // Arrange
        mockTask.setAssignee("agent.smith");

        // Act
        listener.syncToProjection(mockTask);

        // Assert CQRS Sync
        verify(jdbcTemplate, times(1)).update(
            anyString(),
            eq("KANBAN-" + mockTask.getId()),
            eq("KANBAN"),
            eq(mockTask.getId().toString()),
            eq("QA Audit Task"),
            eq("agent.smith"),
            any(),
            eq("OPEN"),
            eq("default"),
            eq(1)
        );

        // Assert Websocket Broadcast
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/workdesk/default"),
            any(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.class)
        );
    }
}
