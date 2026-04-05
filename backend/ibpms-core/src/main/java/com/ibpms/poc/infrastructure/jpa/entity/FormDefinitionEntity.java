package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA inmutable - Almacenamiento histórico y JSONB de diseños de formularios (US-003, CA-87).
 * Utiliza @JdbcTypeCode para soporte nativo de JSONB en PostgreSQL (Hibernate 6+).
 */
@Entity
@Table(name = "ibpms_form_definitions")
public class FormDefinitionEntity {

    @Id
    private UUID id;

    @Column(name = "form_id", nullable = false)
    private UUID formId;

    @Column(name = "version_id", nullable = false)
    private Integer versionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_content", columnDefinition = "jsonb", nullable = false)
    private String schemaContent;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    public FormDefinitionEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFormId() {
        return formId;
    }

    public void setFormId(UUID formId) {
        this.formId = formId;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public String getSchemaContent() {
        return schemaContent;
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
    }
}
