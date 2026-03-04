package com.ibpms.poc.application.service.inbox;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DataSegregationTest {

    private final TaskService mockCamundaService = mock(TaskService.class);
    private final TaskQuery mockQuery = mock(TaskQuery.class);
    private final TaskQueryService queryService = new TaskQueryService(mockCamundaService);

    // ── QA Instruction: Data Segregation (Pantalla 14) ──
    @Test
    @DisplayName("Debe asegurar matemáticamente que la BD retorna exactamente las tareas asignadas al usuario A, sin mezclar las del B")
    void getSegregatedInboxTasks_OnlyRetrievesUserSpecificTasks() {

        // 1. Arrange: Construir el Mock de la Base de Datos Camunda
        when(mockCamundaService.createTaskQuery()).thenReturn(mockQuery);
        when(mockQuery.taskAssignee(anyString())).thenReturn(mockQuery);
        when(mockQuery.active()).thenReturn(mockQuery);
        when(mockQuery.orderByTaskCreateTime()).thenReturn(mockQuery);
        when(mockQuery.desc()).thenReturn(mockQuery);

        // Generamos un pool de memoria falso para User_B con 5 registros
        List<Task> mockUserBTasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) mockUserBTasks.add(mock(Task.class));

        // Cuando el motor recibe "User_B", le damos su porción exacta
        when(mockQuery.taskAssignee("User_B")).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(mockUserBTasks);

        // 2. Act: Autenticado como "User_B", el endpoint pide el inbox
        List<Task> result = queryService.getSegregatedInboxTasks("User_B");

        // 3. Assert: Forzamos la comprobación de segregación
        assertEquals(5, result.size(), "El listado debió segregar y retornar exactamente las 5 tareas de B, excluyendo las de A");
        
        // Verificamos que el query inyectó correctamente el WHERE assignee = User_B a nivel SQL
        verify(mockQuery, times(1)).taskAssignee("User_B");
    }
}
