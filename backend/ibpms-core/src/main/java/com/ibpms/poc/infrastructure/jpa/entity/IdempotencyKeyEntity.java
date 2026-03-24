package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA — tabla ibpms_idempotency_key.
 * Almacena la respuesta serializada de operaciones POST idempotentes.
 */
@Entity
@Table(name = "ibpms_idempotency_key")
public class IdempotencyKeyEntity {

    /**
     * HOTFIX (US-029): Resolución de BeanCreationException en Tomcat 8080.
     * Se renombra el identificador JPA a "idempotency_key" previniendo
     * la falla del ApplicationContext durante el análisis de esquema.
     */
    @Id
    @Column(name = "idempotency_key", length = 36, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "response_payload", columnDefinition = "json")
    private String resultJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String v) {
        this.idempotencyKey = v;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String v) {
        this.resultJson = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }
}
