// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.BpmnAuditPort;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BpmnAuditJpaAdapter implements BpmnAuditPort {

    private final BpmnDesignAuditLogRepository repository;

    public BpmnAuditJpaAdapter(BpmnDesignAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void logAction(UUID processDesignId, String action, String userId, int versionAffected, String details) {
        BpmnDesignAuditLogEntity.Action enumAction;
        try {
            enumAction = BpmnDesignAuditLogEntity.Action.valueOf(action);
        } catch (IllegalArgumentException e) {
            enumAction = BpmnDesignAuditLogEntity.Action.PRE_FLIGHT; // default or handle
        }
        
        BpmnDesignAuditLogEntity entity = new BpmnDesignAuditLogEntity(
            processDesignId,
            enumAction,
            userId,
            versionAffected,
            details
        );
        repository.save(entity);
    }
}
