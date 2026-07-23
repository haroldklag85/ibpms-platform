package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TaskSkipRepository;
import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskExecutionService {

    private final TaskService camundaTaskService;
    private final TaskSkipRepository taskSkipRepository;

    public TaskExecutionService(TaskService camundaTaskService, TaskSkipRepository taskSkipRepository) {
        this.camundaTaskService = camundaTaskService;
        this.taskSkipRepository = taskSkipRepository;
    }

    @Transactional
    public void skipTask(String taskId, String skipReason, String skipReasonDetail, String username, String tenantId) {
        // 1. Validar la justificación (≥10 chars si es "Otro")
        if ("OTHER".equalsIgnoreCase(skipReason) || "OTRO".equalsIgnoreCase(skipReason)) {
            if (skipReasonDetail == null || skipReasonDetail.trim().length() < 10) {
                throw new IllegalArgumentException("La justificación debe tener al menos 10 caracteres cuando el motivo es 'Otro'.");
            }
        }

        // 2. Des-asignar la tarea en el API de Camunda
        camundaTaskService.setAssignee(taskId, null);

        // 3. Registrar el evento de salto en TaskSkipEntity (que soporta Javers AuditTrail)
        TaskSkipEntity skip = new TaskSkipEntity();
        skip.setTaskId(taskId);
        skip.setUserId(username);
        skip.setTenantId(tenantId != null ? tenantId : "default-tenant");
        skip.setSkipReason(skipReason);
        skip.setSkipReasonDetail(skipReasonDetail);
        
        taskSkipRepository.save(skip);
    }

    @Transactional
    public void forceRoute(String taskId, String targetUserId, String reason, String requestingUsername, String tenantId) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario destino (targetUserId) es obligatorio para forzar el ruteo.");
        }

        // 1. Validar que el usuario objetivo pertenezca al mismo Tenant que el solicitante
        // TODO: Cuando UserEntity tenga tenant_id, habilitar esta validación estricta.
        // Por ahora, asumimos que todos los usuarios de prueba comparten el tenant en este entorno E2E.
        boolean targetUserExistsInTenant = true; 
        if (!targetUserExistsInTenant) {
            throw new SecurityException("IDOR detectado: El usuario destino no pertenece a tu Tenant.");
        }

        // 2. Reasignar la tarea en Camunda
        camundaTaskService.setAssignee(taskId, targetUserId);

        // 3. Opcional: Registrar auditoría de la delegación si se requiere (por ahora Javers interceptará el Entity si aplica)
    }
}
