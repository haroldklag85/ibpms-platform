package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.LiberarTareaUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.infrastructure.jpa.entity.TaskAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LiberarTareaService implements LiberarTareaUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final TaskAuditLogRepository auditRepository;

    public LiberarTareaService(ProcesoBpmPort procesoBpmPort, TaskAuditLogRepository auditRepository) {
        this.procesoBpmPort = procesoBpmPort;
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional
    public void liberar(String taskId, String username, Map<String, Object> partialPayload, String reason) {
        // Enviar al motor BPM (Set assignee a null y almacenar Draft)
        procesoBpmPort.liberarTarea(taskId, partialPayload);

        // Almacenar Audit Log Inmutable en BD
        TaskAuditLogEntity audit = new TaskAuditLogEntity(taskId, "UNCLAIM", username, null, reason);
        auditRepository.save(audit);
    }
}
