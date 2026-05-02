package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.out.KanbanColumnPort;
import com.ibpms.poc.domain.model.kanban.KanbanColumn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KanbanColumnServiceTest {

    @Mock
    private KanbanColumnPort kanbanColumnPort;

    @InjectMocks
    private KanbanColumnService kanbanColumnService;

    @Test
    void createColumn_WhenCountLessThan7_ShouldSave() {
        UUID boardId = UUID.randomUUID();
        when(kanbanColumnPort.countByBoardId(boardId)).thenReturn(5L);
        KanbanColumn savedColumn = new KanbanColumn(UUID.randomUUID(), boardId, "NEW_COL", 6);
        when(kanbanColumnPort.save(any(KanbanColumn.class))).thenReturn(savedColumn);

        KanbanColumn result = kanbanColumnService.createColumn(boardId, "NEW_COL");

        assertNotNull(result);
        verify(kanbanColumnPort).save(any(KanbanColumn.class));
    }

    @Test
    void createColumn_WhenCountIs7_ShouldThrowConflict() {
        UUID boardId = UUID.randomUUID();
        when(kanbanColumnPort.countByBoardId(boardId)).thenReturn(7L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> kanbanColumnService.createColumn(boardId, "COL_8"));
        
        assertEquals(409, exception.getStatusCode().value());
        verify(kanbanColumnPort, never()).save(any());
    }

    @Test
    void deleteColumn_ShouldInvokePort() {
        UUID colId = UUID.randomUUID();
        kanbanColumnService.deleteColumn(colId);
        verify(kanbanColumnPort).deleteById(colId);
    }
}
