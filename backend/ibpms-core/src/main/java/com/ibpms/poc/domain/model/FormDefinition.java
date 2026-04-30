package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FormDefinition {
    private UUID id;
    private UUID formId;
    private Integer versionId;
    private String schemaContent;
    private String hashSha256;
    private String createdBy;
    private LocalDateTime createdAt;

    public FormDefinition() {}

    public FormDefinition(UUID id, UUID formId, Integer versionId, String schemaContent, String hashSha256, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.formId = formId;
        this.versionId = versionId;
        this.schemaContent = schemaContent;
        this.hashSha256 = hashSha256;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFormId() { return formId; }
    public void setFormId(UUID formId) { this.formId = formId; }
    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer versionId) { this.versionId = versionId; }
    public String getSchemaContent() { return schemaContent; }
    public void setSchemaContent(String schemaContent) { this.schemaContent = schemaContent; }
    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
