package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.out.IdempotencyPort;
import com.ibpms.poc.application.service.security.SecurityAnomalyService;
import com.ibpms.poc.domain.exception.SoDViolationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CompletarTareaServiceSoDTest {

    private TaskService taskService;
    private HistoryService historyService;
    private IdempotencyPort idempotencyPort;
    private SecurityAnomalyService securityAnomalyService;
    private CompletarTareaService completarTareaService;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        historyService = mock(HistoryService.class);
        idempotencyPort = mock(IdempotencyPort.class);
        securityAnomalyService = mock(SecurityAnomalyService.class);
        completarTareaService = new CompletarTareaService(taskService, historyService, idempotencyPort, securityAnomalyService);
    }

    @Test
    void testCompletar_ThrowsSoDViolation_WhenCreatorIsApprover() {
        String taskId = "task123";
        String processInstanceId = "pi123";
        String username = "johndoe";

        TaskQuery taskQuery = mock(TaskQuery.class);
        Task task = mock(Task.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(taskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getAssignee()).thenReturn(username);
        when(task.getProcessInstanceId()).thenReturn(processInstanceId);

        HistoricProcessInstanceQuery historyQuery = mock(HistoricProcessInstanceQuery.class);
        HistoricProcessInstance hpi = mock(HistoricProcessInstance.class);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historyQuery);
        when(historyQuery.processInstanceId(processInstanceId)).thenReturn(historyQuery);
        when(historyQuery.singleResult()).thenReturn(hpi);
        
        // Simular que el creador (startUserId) es el mismo que el usuario aprobador
        when(hpi.getStartUserId()).thenReturn(username);

        Map<String, Object> vars = new HashMap<>();

        // Ejecución y aserción
        assertThrows(SoDViolationException.class, () -> {
            completarTareaService.completar(taskId, vars, null, username);
        });

        // Verificar que se haya registrado la anomalía de seguridad
        verify(securityAnomalyService, times(1)).registerAnomaly("INTENTO_SOD_AUTOAPROBACION", username, taskId);
        
        // Verificar que NO se completó en Camunda
        verify(taskService, never()).complete(anyString(), any());
    }

    @Test
    void testCompletar_Success_WhenCreatorIsDifferent() {
        String taskId = "task123";
        String processInstanceId = "pi123";
        String approver = "johndoe";
        String creator = "janedoe";

        TaskQuery taskQuery = mock(TaskQuery.class);
        Task task = mock(Task.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(taskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getAssignee()).thenReturn(approver);
        when(task.getProcessInstanceId()).thenReturn(processInstanceId);

        HistoricProcessInstanceQuery historyQuery = mock(HistoricProcessInstanceQuery.class);
        HistoricProcessInstance hpi = mock(HistoricProcessInstance.class);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historyQuery);
        when(historyQuery.processInstanceId(processInstanceId)).thenReturn(historyQuery);
        when(historyQuery.singleResult()).thenReturn(hpi);
        
        // Simular que el creador es diferente
        when(hpi.getStartUserId()).thenReturn(creator);

        Map<String, Object> vars = new HashMap<>();
        vars.put("decision", true);

        // Ejecución (no lanza excepción)
        completarTareaService.completar(taskId, vars, null, approver);

        // Verificar que no se registró anomalía
        verify(securityAnomalyService, never()).registerAnomaly(anyString(), anyString(), anyString());
        
        // Verificar que la tarea SI se completó en Camunda
        verify(taskService, times(1)).complete(eq(taskId), any());
    }
}
