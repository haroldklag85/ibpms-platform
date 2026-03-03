package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.ReasignarTareaUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.domain.exception.TaskReassignmentLimitException;
import com.ibpms.poc.infrastructure.jpa.entity.TaskAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReasignarTareaService implements ReasignarTareaUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final TaskAuditLogRepository auditRepository;

    // N rebotes permitidos antes de lanzar Error BAD_REQUEST de límite superado
    private static final int MAX_REASSIGN_PINGS = 3;

    public ReasignarTareaService(ProcesoBpmPort procesoBpmPort, TaskAuditLogRepository auditRepository) {
        this.procesoBpmPort = procesoBpmPort;
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional
    public void reasignar(String taskId, String currentUsername, String newUserId, String reason) {
        // Consultar el comportamiento errático de Ping-Pong
        long pingPongCount = auditRepository.countByTaskIdAndAction(taskId, "REASSIGN");

        if (pingPongCount >= MAX_REASSIGN_PINGS) {
            throw new TaskReassignmentLimitException(
                    "La tarea superó el límite estricto (" + MAX_REASSIGN_PINGS + ") de reasignaciones permitidas.");
        }

        // Delegar la reasignación en caliente sobre el motor
        procesoBpmPort.reasignarTarea(taskId, newUserId);

        // Guardar Auditoría
        TaskAuditLogEntity audit = new TaskAuditLogEntity(taskId, "REASSIGN", currentUsername, newUserId, reason);
        auditRepository.save(audit);
    }
}
