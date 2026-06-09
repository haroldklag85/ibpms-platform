package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.SystemAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SystemAuditLogRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Aplicación para el Registro de Auditoría del Sistema.
 * Centraliza logs administrativos como manipulaciones a la DLQ.
 * 
 * @Traceability(US = "US-017", CA = {"CA-08"})
 */
@Service
@Transactional
@Traceability(US = "US-017", CA = {"CA-08"})
public class SystemAuditLogService {

    private final SystemAuditLogRepository systemAuditLogRepository;

    public SystemAuditLogService(SystemAuditLogRepository systemAuditLogRepository) {
        this.systemAuditLogRepository = systemAuditLogRepository;
    }

    /**
     * Guarda un log de auditoría persistido en base de datos.
     * @param entity Entidad a guardar.
     * @return La entidad guardada.
     */
    // @Traceability: US-017 - CA-08 (ADR-001 Refactor)
    public SystemAuditLogEntity saveAuditLog(SystemAuditLogEntity entity) {
        return systemAuditLogRepository.save(entity);
    }
}
