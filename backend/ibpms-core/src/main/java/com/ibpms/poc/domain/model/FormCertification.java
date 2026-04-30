package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FormCertification {
    private UUID id;
    private UUID formDefinitionId;
    private Boolean isQaCertified;
    private String certifiedSchemaHash;
    private String certifiedBy;
    private LocalDateTime certifiedAt;

    public FormCertification() {
        this.id = UUID.randomUUID();
        this.isQaCertified = false;
    }

    public FormCertification(UUID id, UUID formDefinitionId, Boolean isQaCertified, String certifiedSchemaHash, String certifiedBy, LocalDateTime certifiedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.formDefinitionId = formDefinitionId;
        this.isQaCertified = isQaCertified != null ? isQaCertified : false;
        this.certifiedSchemaHash = certifiedSchemaHash;
        this.certifiedBy = certifiedBy;
        this.certifiedAt = certifiedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFormDefinitionId() { return formDefinitionId; }
    public void setFormDefinitionId(UUID formDefinitionId) { this.formDefinitionId = formDefinitionId; }
    public Boolean getIsQaCertified() { return isQaCertified; }
    public void setIsQaCertified(Boolean isQaCertified) { this.isQaCertified = isQaCertified; }
    public String getCertifiedSchemaHash() { return certifiedSchemaHash; }
    public void setCertifiedSchemaHash(String certifiedSchemaHash) { this.certifiedSchemaHash = certifiedSchemaHash; }
    public String getCertifiedBy() { return certifiedBy; }
    public void setCertifiedBy(String certifiedBy) { this.certifiedBy = certifiedBy; }
    public LocalDateTime getCertifiedAt() { return certifiedAt; }
    public void setCertifiedAt(LocalDateTime certifiedAt) { this.certifiedAt = certifiedAt; }
}
