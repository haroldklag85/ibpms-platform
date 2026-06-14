package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.application.port.out.ClaimAuditPort;
import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import com.ibpms.poc.infrastructure.jpa.entity.ClaimAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ClaimAuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClaimAuditJpaAdapter implements ClaimAuditPort {

    private final ClaimAuditLogRepository repository;

    public ClaimAuditJpaAdapter(ClaimAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ClaimAuditLog auditLog) {
        ClaimAuditLogEntity entity = new ClaimAuditLogEntity();
        entity.setId(auditLog.getId());
        entity.setTaskId(auditLog.getTaskId());
        entity.setUserId(auditLog.getUserId());
        entity.setActionType(auditLog.getActionType());
        entity.setTenantId(auditLog.getTenantId());
        entity.setTimestamp(auditLog.getTimestamp());
        entity.setPreviousAssignee(auditLog.getPreviousAssignee());
        entity.setReason(auditLog.getReason());
        entity.setMessage(auditLog.getMessage());
        
        repository.save(entity);
    }

    @Override
    public List<ClaimAuditLog> findByTaskId(UUID taskId) {
        return repository.findByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ClaimAuditLog toDomain(ClaimAuditLogEntity entity) {
        ClaimAuditLog domain = new ClaimAuditLog(
                entity.getTaskId(),
                entity.getUserId(),
                entity.getActionType(),
                entity.getTenantId(),
                entity.getTimestamp(),
                entity.getPreviousAssignee(),
                entity.getReason(),
                entity.getMessage()
        );
        domain.setId(entity.getId());
        return domain;
    }
}
