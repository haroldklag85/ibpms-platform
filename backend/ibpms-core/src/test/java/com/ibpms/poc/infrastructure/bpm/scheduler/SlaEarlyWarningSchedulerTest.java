package com.ibpms.poc.infrastructure.bpm.scheduler;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlaEarlyWarningSchedulerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private Task mockTask;

    @InjectMocks
    private SlaEarlyWarningScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.singletonList(mockTask));
        
        when(mockTask.getId()).thenReturn("task-123");
        when(mockTask.getProcessInstanceId()).thenReturn("pi-123");
    }

    private void mockTaskTime(double simulatedPercentageConsumed) {
        long now = System.currentTimeMillis();
        // Supongamos un total de 1,000,000 ms
        long totalMs = 1_000_000L;
        long elapsedMs = (long) (totalMs * simulatedPercentageConsumed);
        
        long createMs = now - elapsedMs;
        long dueMs = createMs + totalMs;

        when(mockTask.getCreateTime()).thenReturn(new Date(createMs));
        when(mockTask.getDueDate()).thenReturn(new Date(dueMs));
    }

    @Test
    @DisplayName("CA-6: Boundary Threshold 80.00% - Should flag as at risk")
    void testBoundary80Percent_FlagsAtRisk() {
        mockTaskTime(0.80);
        when(taskService.getVariableLocal("task-123", "isSlaAtRisk")).thenReturn(null);

        scheduler.scanAndFlagAtRiskTasks();

        verify(taskService, times(1)).setVariableLocal("task-123", "isSlaAtRisk", true);
        verify(eventPublisher, times(1)).publishEvent(any(SlaAtRiskEvent.class));
    }

    @Test
    @DisplayName("CA-6: Negative 50% - Should NOT flag as at risk")
    void testNegative50Percent_DoesNotFlag() {
        mockTaskTime(0.50);
        when(taskService.getVariableLocal("task-123", "isSlaAtRisk")).thenReturn(null);

        scheduler.scanAndFlagAtRiskTasks();

        verify(taskService, never()).setVariableLocal(anyString(), anyString(), anyBoolean());
        verify(eventPublisher, never()).publishEvent(any(SlaAtRiskEvent.class));
    }

    @Test
    @DisplayName("CA-6: Positive 80.01% - Should flag as at risk")
    void testPositive80_01_Percent_FlagsAtRisk() {
        mockTaskTime(0.8001);
        when(taskService.getVariableLocal("task-123", "isSlaAtRisk")).thenReturn(null);

        scheduler.scanAndFlagAtRiskTasks();

        verify(taskService, times(1)).setVariableLocal("task-123", "isSlaAtRisk", true);
        verify(eventPublisher, times(1)).publishEvent(any(SlaAtRiskEvent.class));
    }

    @Test
    @DisplayName("CA-6 OBS-3: Idempotency - Should ignore already flagged tasks")
    void testIdempotency_AlreadyFlagged_Ignored() {
        when(mockTask.getCreateTime()).thenReturn(new Date());
        when(mockTask.getDueDate()).thenReturn(new Date());
        // Mock getId to avoid NPE
        
        // Simular que ya está flaggeada
        when(taskService.getVariableLocal("task-123", "isSlaAtRisk")).thenReturn(true);

        scheduler.scanAndFlagAtRiskTasks();

        // Verificar que no se volvió a invocar setVariableLocal ni publishEvent
        verify(taskService, never()).setVariableLocal(anyString(), anyString(), any());
        verify(eventPublisher, never()).publishEvent(any(SlaAtRiskEvent.class));
    }
}
