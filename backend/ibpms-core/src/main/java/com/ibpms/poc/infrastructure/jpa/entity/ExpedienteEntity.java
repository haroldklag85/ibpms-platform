package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA — tabla ibpms_case.
 * Solo existe en la capa de infraestructura. El dominio ignora JPA.
 */
@Entity
@Table(name = "ibpms_case")
public class ExpedienteEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false, columnDefinition = "bpchar")
    private String id;

    @Column(name = "definition_key", nullable = false)
    private String definitionKey;

    @Column(name = "business_key", nullable = false, unique = true)
    private String businessKey;

    @Column(name = "type")
    private String type;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @Column(name = "process_instance_id", length = 36, columnDefinition = "bpchar")
    private String processInstanceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public void setDefinitionKey(String v) {
        this.definitionKey = v;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String v) {
        this.businessKey = v;
    }

    public String getType() {
        return type;
    }

    public void setType(String v) {
        this.type = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String v) {
        this.payload = v;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String v) {
        this.processInstanceId = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }
}
