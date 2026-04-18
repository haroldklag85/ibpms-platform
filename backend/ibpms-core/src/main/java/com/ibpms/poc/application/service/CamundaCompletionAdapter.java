package com.ibpms.poc.application.service;

import org.camunda.bpm.engine.TaskService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CamundaCompletionAdapter {

    private final TaskService taskService;

    public CamundaCompletionAdapter(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * CA-02: DTO minificado a Camunda (SOLO variables de gateway).
     * CA-10: Rollback Saga con 3 retries (1s, 2s, 4s) en caso de fallos del motor local/remoto.
     */
    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void completeTaskInEngine(String taskId, Map<String, Object> gatewayVariables) {
        // Enviar solo las gatewayVariables, ignorando el payload JSONB pesado
        if (gatewayVariables == null || gatewayVariables.isEmpty()) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, gatewayVariables);
        }
    }
}
