package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.SystemAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SystemAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemAuditLogService {

    private final SystemAuditLogRepository systemAuditLogRepository;

    public SystemAuditLogService(SystemAuditLogRepository systemAuditLogRepository) {
        this.systemAuditLogRepository = systemAuditLogRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public SystemAuditLogEntity saveAuditLog(SystemAuditLogEntity entity) {
        return systemAuditLogRepository.save(entity);
    }
}
