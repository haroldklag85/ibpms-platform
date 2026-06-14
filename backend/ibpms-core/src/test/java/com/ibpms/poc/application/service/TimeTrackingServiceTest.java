package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.application.port.out.TimeLogPort;
import com.ibpms.poc.domain.model.kanban.KanbanState;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeTrackingServiceTest {

    @Mock
    private TimeLogPort timeLogPort;

    @Mock
    private KanbanTaskPort kanbanTaskPort;

    @InjectMocks
    private TimeTrackingService timeTrackingService;

    private KanbanTask task;
    private TimeLogEntry entry;

    @BeforeEach
    void setUp() {
        task = new KanbanTask();
        task.setId(UUID.randomUUID());
        task.setStatus(KanbanState.TODO);

        entry = new TimeLogEntry(UUID.randomUUID(), task.getId(), "TASK_AGILE", ZonedDateTime.now(), null, null, "user1", ZonedDateTime.now());
    }

    @Test
    void startTimer_InTodo_ShouldThrowException() {
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> timeTrackingService.startTimer(task.getId(), "TASK_AGILE", "user1"));
        
        assertTrue(exception.getReason().contains("No se puede iniciar el timer en estado TODO o DONE"));
    }

    @Test
    void startTimer_InDone_ShouldThrowException() {
        task.setStatus(KanbanState.DONE);
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> timeTrackingService.startTimer(task.getId(), "TASK_AGILE", "user1"));
        
        assertTrue(exception.getReason().contains("No se puede iniciar el timer en estado TODO o DONE"));
    }

    @Test
    void startTimer_InProgress_ShouldSave() {
        task.setStatus(KanbanState.IN_PROGRESS);
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));
        when(timeLogPort.findActiveByUserAndReference("user1", task.getId())).thenReturn(Optional.empty());
        when(timeLogPort.save(any(TimeLogEntry.class))).thenReturn(entry);

        TimeLogEntry result = timeTrackingService.startTimer(task.getId(), "TASK_AGILE", "user1");

        assertNotNull(result);
        verify(timeLogPort).save(any(TimeLogEntry.class));
    }

    @Test
    void startTimer_WithActiveTimer_ShouldThrowConflict() {
        task.setStatus(KanbanState.IN_PROGRESS);
        when(kanbanTaskPort.findById(task.getId())).thenReturn(Optional.of(task));
        when(timeLogPort.findActiveByUserAndReference("user1", task.getId())).thenReturn(Optional.of(entry));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> timeTrackingService.startTimer(task.getId(), "TASK_AGILE", "user1"));
        
        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void stopTimer_ShouldCalculateDuration() {
        ZonedDateTime start = ZonedDateTime.now().minusMinutes(30);
        entry = new TimeLogEntry(UUID.randomUUID(), task.getId(), "TASK_AGILE", start, null, null, "user1", ZonedDateTime.now());
        
        when(timeLogPort.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(timeLogPort.save(any(TimeLogEntry.class))).thenReturn(entry);

        TimeLogEntry result = timeTrackingService.stopTimer(entry.getId(), "user1");

        assertNotNull(result.getStoppedAt());
        assertEquals(30, result.getDurationMinutes());
        verify(timeLogPort).save(entry);
    }
}
