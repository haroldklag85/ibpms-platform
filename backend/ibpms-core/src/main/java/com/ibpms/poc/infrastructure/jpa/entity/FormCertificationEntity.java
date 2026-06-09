package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_form_certifications")
public class FormCertificationEntity {

    @Id
    private UUID id;

    @Column(name = "form_definition_id", nullable = false)
    private UUID formDefinitionId;

    @Column(name = "is_qa_certified", nullable = false)
    private Boolean isQaCertified = false;

    @Column(name = "certified_schema_hash", length = 64)
    private String certifiedSchemaHash;

    @Column(name = "certified_by", length = 100)
    private String certifiedBy;

    @Column(name = "certified_at")
    private LocalDateTime certifiedAt;

    public FormCertificationEntity() {
        this.id = UUID.randomUUID();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(UUID formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public Boolean getIsQaCertified() {
        return isQaCertified;
    }

    public void setIsQaCertified(Boolean isQaCertified) {
        this.isQaCertified = isQaCertified;
    }

    public String getCertifiedSchemaHash() {
        return certifiedSchemaHash;
    }

    public void setCertifiedSchemaHash(String certifiedSchemaHash) {
        this.certifiedSchemaHash = certifiedSchemaHash;
    }

    public String getCertifiedBy() {
        return certifiedBy;
    }

    public void setCertifiedBy(String certifiedBy) {
        this.certifiedBy = certifiedBy;
    }

    public LocalDateTime getCertifiedAt() {
        return certifiedAt;
    }

    public void setCertifiedAt(LocalDateTime certifiedAt) {
        this.certifiedAt = certifiedAt;
    }
}
