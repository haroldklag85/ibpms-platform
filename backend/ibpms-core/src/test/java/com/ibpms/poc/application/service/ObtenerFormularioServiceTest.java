package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.FormSchemaDTO;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObtenerFormularioServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private Task mockTask;

    @InjectMocks
    private ObtenerFormularioService service;

    private final String taskId = "task-123";

    @BeforeEach
    void setUp() {
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(taskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);
        when(mockTask.getName()).thenReturn("Revisar Documento");
        when(mockTask.getFormKey()).thenReturn("frm_test");
    }

    // ── QA Instruction 5: Test Data Binding (CA-27) ──
    @Test
    @DisplayName("Debe inyectar las variables de proceso (ej. customer_id) recuperadas de Camunda como defaultValues en el FormSchema")
    void obtenerFormulario_inyectaVariablesComoValuesBase() {
        // Arrange
        Map<String, Object> camundaVars = new HashMap<>();
        camundaVars.put("customer_id", "CC-112233"); // Variable ingresada por Etapa 1
        camundaVars.put("otra_variable", "invisible");

        when(taskService.getVariables(taskId)).thenReturn(camundaVars);

        // Act
        FormSchemaDTO schema = service.obtenerFormulario(taskId);

        // Assert
        assertEquals("frm_test", schema.getFormId());

        var compCustomer = schema.getComponents().stream()
                .filter(c -> c.getId().equals("customer_id"))
                .findFirst().orElseThrow();

        assertEquals("CC-112233", compCustomer.getDefaultValue(),
                "El componente debió tomar el defaultValue prellenado de la variable Camunda.");
    }
}
