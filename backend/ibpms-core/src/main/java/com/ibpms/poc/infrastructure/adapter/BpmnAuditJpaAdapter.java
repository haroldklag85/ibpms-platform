// @Traceability: US-005, CA-42 - Activity Timeline
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.BpmnAuditPort;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.domain.model.BpmnDesignAuditEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public List<BpmnDesignAuditEntry> getAuditLogsForProcess(UUID processDesignId) {
        return repository.findByProcessDesignIdOrderByTimestampDesc(processDesignId).stream()
                .map(entity -> new BpmnDesignAuditEntry(
                    entity.getId(),
                    entity.getProcessDesignId(),
                    BpmnDesignAuditEntry.Action.valueOf(entity.getAction().name()),
                    entity.getUserId(),
                    entity.getTimestamp(),
                    entity.getVersionAffected(),
                    entity.getDetails()
                ))
                .collect(Collectors.toList());
    }
}
