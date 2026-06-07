package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanColumnRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanColumnEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KanbanIntegrationServiceTest {

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

    @InjectMocks
    private KanbanBoardService kanbanBoardService;

    private UUID kanbanTaskId;
    private KanbanTaskEntity kanbanTask;
    private KanbanBoardEntity board;
    private String originalTaskId;

    @BeforeEach
    void setUp() {
        kanbanTaskId = UUID.randomUUID();
        originalTaskId = UUID.randomUUID().toString();
        board = new KanbanBoardEntity();
        board.setId(UUID.randomUUID());

        kanbanTask = new KanbanTaskEntity();
        kanbanTask.setId(kanbanTaskId);
        kanbanTask.setBoard(board);
        kanbanTask.setOriginalTaskId(originalTaskId);
        kanbanTask.setStatus("TODO");
    }

    @Test
    void testMoveTaskToInProgressClaimsInWorkdesk() {
        when(taskRepository.findById(kanbanTaskId)).thenReturn(Optional.of(kanbanTask));
        when(columnRepository.findByBoardId(board.getId())).thenReturn(List.of());

        KanbanTaskEntity result = kanbanBoardService.moveTask(kanbanTaskId, "IN_PROGRESS", "jdoe", null);

        assertEquals("IN_PROGRESS", result.getStatus());
        verify(agileTaskService).claimTask(UUID.fromString(originalTaskId), "jdoe");
        verify(columnRepository).save(any(KanbanColumnEntity.class)); // Verifies column creation for auto-escalability
    }

    @Test
    void testMoveTaskToTodoUnclaimsInWorkdesk() {
        when(taskRepository.findById(kanbanTaskId)).thenReturn(Optional.of(kanbanTask));
        when(columnRepository.findByBoardId(board.getId())).thenReturn(List.of());

        KanbanTaskEntity result = kanbanBoardService.moveTask(kanbanTaskId, "TODO", "jdoe", null);

        assertEquals("TODO", result.getStatus());
        verify(agileTaskService).unclaimTask(UUID.fromString(originalTaskId), "jdoe", null);
    }

    @Test
    void testMoveToCustomStateCreatesColumn() {
        when(taskRepository.findById(kanbanTaskId)).thenReturn(Optional.of(kanbanTask));
        when(columnRepository.findByBoardId(board.getId())).thenReturn(List.of()); // No columns configured

        kanbanBoardService.moveTask(kanbanTaskId, "CUSTOM_STATE", "jdoe", null);

        verify(columnRepository).save(argThat(col -> col.getName().equals("CUSTOM_STATE")));
    }

    @Test
    void testGetBoardColumnsMergesRealData() {
        when(taskRepository.findByBoardIdOrderByUpdatedAtDesc(board.getId())).thenReturn(List.of(kanbanTask));
        
        WorkdeskProjectionEntity realTask = new WorkdeskProjectionEntity();
        realTask.setId(originalTaskId);
        realTask.setOriginalTaskId(originalTaskId);
        realTask.setTitle("Test Title");
        realTask.setAssignee("jdoe");
        
        when(projectionRepository.findAllById(List.of(originalTaskId))).thenReturn(List.of(realTask));
        when(columnRepository.findByBoardId(board.getId())).thenReturn(List.of());

        Map<String, List<Map<String, Object>>> response = kanbanBoardService.getBoardColumns("tenant1", board.getId());

        assertNotNull(response);
        List<Map<String, Object>> columns = response.get("columns");
        
        // Find the "TODO" column
        Map<String, Object> todoCol = columns.stream().filter(c -> "TODO".equals(c.get("name"))).findFirst().orElseThrow();
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) todoCol.get("tasks");
        
        assertEquals(1, tasks.size());
        Map<String, Object> taskMap = tasks.get(0);
        assertEquals("Test Title", taskMap.get("title"));
        assertEquals("jdoe", taskMap.get("assignee"));
    }
}
