package com.ibpms.poc.application.service.ai;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;

class GenerativeTaskWorkerTest {

    private final MockLlmClient mockLlmClient = mock(MockLlmClient.class);
    private final GenerativeTaskWorker worker = new GenerativeTaskWorker(mockLlmClient);

    @Test
    @DisplayName("El agente ExternalTask asíncrono debe invocar el LLM e inmediatamente ejecutar externalTaskService.complete()")
    void execute_CallsLlmAndCompletesTask() {
        // Arrange
        ExternalTask mockTask = mock(ExternalTask.class);
        ExternalTaskService mockService = mock(ExternalTaskService.class);

        when(mockTask.getVariable("promptId")).thenReturn("SYS_01");
        when(mockTask.getVariable("inputData")).thenReturn("Analiza este contrato");

        when(mockLlmClient.infer("SYS_01", "Analiza este contrato"))
                .thenReturn("Respuesta del LLM");

        // Act
        worker.execute(mockTask, mockService);

        // Assert
        verify(mockLlmClient, times(1)).infer("SYS_01", "Analiza este contrato");

        Map<String, Object> expectedVariables = Collections.singletonMap("aiResponse", "Respuesta del LLM");
        verify(mockService, times(1)).complete(mockTask, expectedVariables);

        // El hilo primario nunca fue bloqueado síncronamente.
        verify(mockService, never()).handleFailure(any(ExternalTask.class), anyString(), anyString(), anyInt(),
                anyLong());
    }
}
