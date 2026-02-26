package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.out.IdempotencyPort;
import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CompletarTareaService implements CompletarTareaUseCase {

    private final TaskService taskService;
    private final IdempotencyPort idempotencyPort;

    public CompletarTareaService(TaskService taskService, IdempotencyPort idempotencyPort) {
        this.taskService = taskService;
        this.idempotencyPort = idempotencyPort;
    }

    @Override
    @Transactional
    public void completar(String taskId, Map<String, Object> variables, String idempotencyKey) {

        // Prevención de doble Submit en la UI (Idempotencia)
        if (idempotencyKey != null && idempotencyPort.existe(idempotencyKey)) {
            return; // Ya fue completada por el mismo clic doble
        }

        // Delegar al motor BPM. Por ser Shared Transaction Manager,
        // cualquier Exception hará rollback tanto de Camunda como de Ibpm
        taskService.complete(taskId, variables);

        if (idempotencyKey != null) {
            // El resultado "OK" vacío porque devuelve 204 No Content
            idempotencyPort.registrar(idempotencyKey, "{}");
        }
    }
}
