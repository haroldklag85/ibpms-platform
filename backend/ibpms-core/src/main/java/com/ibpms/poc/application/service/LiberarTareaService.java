package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.LiberarTareaUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.application.port.out.ClaimAuditPort;
import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LiberarTareaService implements LiberarTareaUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final ClaimAuditPort claimAuditPort;

    public LiberarTareaService(ProcesoBpmPort procesoBpmPort, ClaimAuditPort claimAuditPort) {
        this.procesoBpmPort = procesoBpmPort;
        this.claimAuditPort = claimAuditPort;
    }

    @Override
    @Transactional
    public void liberar(String taskId, String username, Map<String, Object> partialPayload, String reason) {
        // Enviar al motor BPM (Set assignee a null y sin partialPayload - Amnesia Transaccional)
        procesoBpmPort.liberarTarea(taskId, null);

        // Almacenar Audit Log Inmutable en BD
        ClaimAuditLog audit = new ClaimAuditLog(UUID.fromString(taskId), username, "RELEASED", "default", Instant.now(), null, reason, null);
        claimAuditPort.save(audit);
    }
}
