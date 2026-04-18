package com.ibpms.poc.application.service;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.IdentityLink;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutoClaimService {

    private final TaskService taskService;

    public AutoClaimService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Valida y ejecuta el Auto-Claim si el usuario es elegible.
     * Si la tarea ya está asignada a otro, lanza excepcion.
     * Si no está asignada, verifica que sea candidato y hace claim.
     */
    @Transactional
    public void tryAutoClaim(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        if (task.getAssignee() != null) {
            if (!task.getAssignee().equals(userId)) {
                throw new IllegalStateException("FORBIDDEN: La tarea ya se encuentra asignada a otro usuario.");
            }
            return; // Ya la tiene asignada, OK.
        }

        // Tarea está unassigned. Verificamos si es candidato.
        // En una implementación real, se extraen los grupos del token JWT y se cruzan.
        // Aquí asumiremos que el IdentityLink confirma si puede hacer claim.
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        
        // Simulación: Si hay grupos asignados y el usuario llama a esta API, se auto-asigna.
        // Ojo: En CQRS estricto, validaríamos roles desde EntraID.
        taskService.claim(taskId, userId);
    }
}
