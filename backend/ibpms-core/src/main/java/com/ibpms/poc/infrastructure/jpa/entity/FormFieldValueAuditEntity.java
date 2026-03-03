package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA - Auditoría a Nivel de Campo (CA-12).
 * Control de modificaciones granulares de las variables Camunda.
 */
@Entity
@Table(name = "ibpms_form_field_value_audit")
public class FormFieldValueAuditEntity {

    @Id
    private UUID id;

    @Column(name = "process_instance_id", nullable = false, length = 64)
    private String processInstanceId;

    @Column(name = "form_design_id", nullable = false)
    private UUID formDesignId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public FormFieldValueAuditEntity() {
        this.id = UUID.randomUUID();
        this.changedAt = LocalDateTime.now();
    }

    public FormFieldValueAuditEntity(String processInstanceId, UUID formDesignId, String fieldName,
            String oldValue, String newValue, String changedBy) {
        this();
        this.processInstanceId = processInstanceId;
        this.formDesignId = formDesignId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public UUID getFormDesignId() {
        return formDesignId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
