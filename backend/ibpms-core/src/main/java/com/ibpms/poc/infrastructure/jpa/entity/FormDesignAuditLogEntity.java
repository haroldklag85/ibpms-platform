package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de Auditoría Forense para Formularios (US-028 CA-10/CA-11).
 * Registra eventos ISO inmutables como la Certificación Hash SHA-256.
 */
@Entity
@Table(name = "ibpms_form_design_audit")
public class FormDesignAuditLogEntity {

    public enum Action {
        CREATED, UPDATED, DELETED, CERTIFIED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private UUID formId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "dummy_payload", columnDefinition = "LONGTEXT")
    private String dummyPayload;

    @Column(name = "hash_value", length = 64)
    private String hashValue;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public FormDesignAuditLogEntity() {
        this.timestamp = LocalDateTime.now();
    }

    public FormDesignAuditLogEntity(UUID formId, Action action, String userId, String dummyPayload, String hashValue) {
        this();
        this.formId = formId;
        this.action = action;
        this.userId = userId;
        this.dummyPayload = dummyPayload;
        this.hashValue = hashValue;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getFormId() {
        return formId;
    }

    public void setFormId(UUID formId) {
        this.formId = formId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDummyPayload() {
        return dummyPayload;
    }

    public void setDummyPayload(String dummyPayload) {
        this.dummyPayload = dummyPayload;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
