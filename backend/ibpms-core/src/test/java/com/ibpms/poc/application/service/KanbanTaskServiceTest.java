// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.AuditLogUseCase;
import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.domain.model.kanban.KanbanState;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KanbanTaskServiceTest {

    @Mock
    private KanbanTaskPort kanbanTaskPort;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AuditLogUseCase auditLogService;

    @InjectMocks
    private KanbanTaskService kanbanTaskService;

    private KanbanTask task;

    @BeforeEach
    void setUp() {
        task = new KanbanTask();
        task.setId(UUID.randomUUID());
        task.setBoardId(UUID.randomUUID());
        task.setStatus(KanbanState.TODO);
    }

    @Test
    void moveTask_ValidTransition_ShouldSaveAndEmitEvent() {
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        kanbanTaskService.moveTask(task.getId(), "IN_PROGRESS", null, "user1");

        assertEquals(KanbanState.IN_PROGRESS, task.getStatus());
        verify(kanbanTaskPort).save(task);
        verify(messagingTemplate).convertAndSend(eq("/topic/kanban/" + task.getBoardId() + "/tasks"), eq(task));
        verify(auditLogService).commit(eq("user1"), eq(task));
    }

    @Test
    void moveTask_InvalidTransition_ShouldThrowException() {
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> kanbanTaskService.moveTask(task.getId(), "DONE", null, "user1"));
        
        assertTrue(exception.getReason().contains("Transición inválida"));
        verify(kanbanTaskPort, never()).save(any());
    }

    @Test
    void moveTask_DoneIsImmutable_ShouldThrowException() {
        task.setStatus(KanbanState.DONE);
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> kanbanTaskService.moveTask(task.getId(), "TODO", null, "user1"));
        
        assertTrue(exception.getReason().contains("Tarea en DONE es inmutable"));
    }

    @Test
    void moveTask_BlockedWithoutReason_ShouldThrowException() {
        task.setStatus(KanbanState.IN_PROGRESS);
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> kanbanTaskService.moveTask(task.getId(), "BLOCKED", "", "user1"));
        
        assertTrue(exception.getReason().contains("Se requiere una razón para bloquear"));
    }
}
