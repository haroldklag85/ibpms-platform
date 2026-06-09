package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.FormFieldValueAuditEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormFieldValueAuditRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de auditoría para valores de formulario.
 * 
 * @Traceability(US = "US-008", CA = {"CA-01"})
 */
@Service
@Transactional
@Traceability(US = "US-008", CA = {"CA-01"})
public class FormFieldValueAuditService {

    private final FormFieldValueAuditRepository auditRepository;

    public FormFieldValueAuditService(FormFieldValueAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Guarda un registro de auditoría de un campo de formulario modificado.
     * @param entity Entidad de auditoría.
     * @return La entidad guardada.
     */
    // @Traceability: US-008 - CA-01 (ADR-001 Refactor)
    public FormFieldValueAuditEntity saveAudit(FormFieldValueAuditEntity entity) {
        return auditRepository.save(entity);
    }
}
