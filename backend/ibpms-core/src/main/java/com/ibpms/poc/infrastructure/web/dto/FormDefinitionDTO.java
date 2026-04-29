package com.ibpms.poc.infrastructure.web.dto;

import com.ibpms.poc.infrastructure.jpa.entity.FormCertificationEntity;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class FormDefinitionDTO {
    private UUID id;
    private UUID formId;
    private Integer versionId;
    private String schemaContent;
    private String createdBy;
    private LocalDateTime createdAt;
    private String hashSha256;

    // QA Certification fields
    private Boolean isQaCertified;
    private String certifiedSchemaHash;
    private String certifiedBy;
    private LocalDateTime certifiedAt;

    public FormDefinitionDTO() {
    }

    public static FormDefinitionDTO from(FormDefinitionEntity def, FormCertificationEntity cert) {
        FormDefinitionDTO dto = new FormDefinitionDTO();
        dto.setId(def.getId());
        dto.setFormId(def.getFormId());
        dto.setVersionId(def.getVersionId());
        dto.setSchemaContent(def.getSchemaContent());
        dto.setCreatedBy(def.getCreatedBy());
        dto.setCreatedAt(def.getCreatedAt());
        dto.setHashSha256(def.getHashSha256());

        if (cert != null) {
            dto.setIsQaCertified(cert.getIsQaCertified());
            dto.setCertifiedSchemaHash(cert.getCertifiedSchemaHash());
            dto.setCertifiedBy(cert.getCertifiedBy());
            dto.setCertifiedAt(cert.getCertifiedAt());
        } else {
            dto.setIsQaCertified(false);
        }
        return dto;
    }

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

    public Boolean getIsQaCertified() {
        return isQaCertified;
    }

    public void setIsQaCertified(Boolean qaCertified) {
        isQaCertified = qaCertified;
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
