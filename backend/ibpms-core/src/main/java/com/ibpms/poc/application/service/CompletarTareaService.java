package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.out.IdempotencyPort;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import com.ibpms.poc.domain.exception.TaskOwnershipViolationException;
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
    public void completar(String taskId, Map<String, Object> variables, String idempotencyKey, String username) {

        // Prevención de doble Submit en la UI (Idempotencia)
        if (idempotencyKey != null && idempotencyPort.existe(idempotencyKey)) {
            return; // Ya fue completada por el mismo clic doble
        }

        // US-029 Implicit Locking (Zero-Trust)
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            String assignee = task.getAssignee();
            if (assignee != null && username != null && !assignee.equals(username)) {
                throw new TaskOwnershipViolationException("Acceso denegado: La tarea pertenece a otro usuario registrado.");
            }
        }

        // Delegar al motor BPM. Por ser Shared Transaction Manager,
        // cualquier Exception hará rollback tanto de Camunda como de Ibpm
        Map<String, Object> gatewayVars = extractGatewayVariables(variables);
        taskService.complete(taskId, gatewayVars);

        if (idempotencyKey != null) {
            // El resultado "OK" vacío porque devuelve 204 No Content
            idempotencyPort.registrar(idempotencyKey, "{}");
        }
    }

    private Map<String, Object> extractGatewayVariables(Map<String, Object> allVariables) {
        if (allVariables == null) return java.util.Collections.emptyMap();
        
        // Filtra solo variables orientadas a routing/gateways 
        // (excluyendo Data Grids, Arrays PII y blobs de texto largo)
        java.util.Map<String, Object> filtered = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : allVariables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Convención de exclusión de topología (No pasar metadata UI, objects ni arrays a Camunda)
            if (key.startsWith("_") || value instanceof java.util.Collection || value instanceof java.util.Map) {
                continue;
            }
            // Mantiene booleanos, números y strings cortos/decisiones explícitas
            if (value instanceof Boolean || value instanceof Number || key.startsWith("decision_") || key.startsWith("gw_") || key.endsWith("Action")) {
                filtered.put(key, value);
            } else if (value instanceof String && ((String) value).length() < 255) {
                filtered.put(key, value);
            }
        }
        return filtered;
    }
}
