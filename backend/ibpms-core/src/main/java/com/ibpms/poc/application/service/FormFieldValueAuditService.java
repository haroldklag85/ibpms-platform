package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.FormFieldValueAuditEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormFieldValueAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FormFieldValueAuditService {

    private final FormFieldValueAuditRepository auditRepository;

    public FormFieldValueAuditService(FormFieldValueAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public FormFieldValueAuditEntity saveAudit(FormFieldValueAuditEntity entity) {
        return auditRepository.save(entity);
    }
}
