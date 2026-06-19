package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.KanbanBoardDto;
import com.ibpms.poc.application.dto.KanbanTaskStateDto;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanColumnRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KanbanIntegrationServiceTest {

    @Mock
    private KanbanTaskRepository taskRepository;
    @Mock
    private KanbanBoardRepository boardRepository;
    @Mock
    private WorkdeskProjectionRepository projectionRepository;
    @Mock
    private AgileTaskService agileTaskService;
    @Mock
    private KanbanColumnRepository columnRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KanbanBoardService boardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetKanbanBoardReturnsRealTasks() {
        String projectId = UUID.randomUUID().toString();
        
        KanbanTaskEntity kt = new KanbanTaskEntity();
        kt.setId(UUID.randomUUID());
        kt.setOriginalTaskId(UUID.randomUUID().toString());
        
        WorkdeskProjectionEntity wp = new WorkdeskProjectionEntity();
        wp.setId(kt.getOriginalTaskId());
        wp.setTitle("Test Task");
        wp.setStatus("PENDING");

        when(taskRepository.findByBoardIdOrderByUpdatedAtDesc(UUID.fromString(projectId))).thenReturn(List.of(kt));
        when(projectionRepository.findAllById(List.of(kt.getOriginalTaskId()))).thenReturn(List.of(wp));
        // @Traceability: BUG-FIX: Corrección String a UUID
        when(columnRepository.findByBoardId(UUID.fromString(projectId))).thenReturn(List.of());

        KanbanBoardDto result = boardService.getKanbanBoard("default", projectId);

        assertNotNull(result);
        assertEquals(3, result.getColumns().size());
        
        var todoColumn = result.getColumns().stream().filter(c -> c.getName().equals("TODO")).findFirst().get();
        assertEquals(1, todoColumn.getTasks().size());
        assertEquals("Test Task", todoColumn.getTasks().get(0).getTitle());
    }

    @Test
    void testPatchTaskStateToInProgressCallsClaim() {
        String taskId = UUID.randomUUID().toString();
        String projectId = UUID.randomUUID().toString();
        
        KanbanTaskEntity kt = new KanbanTaskEntity();
        kt.setId(UUID.fromString(taskId));
        kt.setOriginalTaskId(UUID.randomUUID().toString());
        
        when(taskRepository.findById(UUID.fromString(taskId))).thenReturn(Optional.of(kt));

        KanbanTaskStateDto response = boardService.moveTask(projectId, taskId, "IN_PROGRESS", "user123");

        assertEquals("IN_PROGRESS", response.getStatus());
        verify(agileTaskService, times(1)).claimTask(UUID.fromString(kt.getOriginalTaskId()), "user123");
    }

    @Test
    void testPatchTaskStateEmitsWebsocketEvent() {
        String taskId = UUID.randomUUID().toString();
        String projectId = UUID.randomUUID().toString();
        
        KanbanTaskEntity kt = new KanbanTaskEntity();
        kt.setId(UUID.fromString(taskId));
        kt.setOriginalTaskId(UUID.randomUUID().toString());
        
        when(taskRepository.findById(UUID.fromString(taskId))).thenReturn(Optional.of(kt));

        boardService.moveTask(projectId, taskId, "TODO", "user123");

        ArgumentCaptor<KanbanTaskStateDto> captor = ArgumentCaptor.forClass(KanbanTaskStateDto.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/workdesk/kanban"), captor.capture());
        
        assertEquals("TODO", captor.getValue().getStatus());
        verify(agileTaskService, times(1)).unclaimTask(eq(UUID.fromString(kt.getOriginalTaskId())), eq("user123"), isNull());
    }
}
