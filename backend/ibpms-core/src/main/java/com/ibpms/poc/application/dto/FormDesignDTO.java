package com.ibpms.poc.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FormDesignDTO {
    private UUID id;
    private String name;
    private String technicalName;
    private String pattern;
    private String status;
    private Integer version;
    private String vueTemplate;
    private String zodSchema;
    private String authorId;
    private LocalDateTime updatedAt;
    private List<FormFieldMetadataDTO> formFields;

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public String getPattern() {
        return pattern;
    }

    public String getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }

    public String getVueTemplate() {
        return vueTemplate;
    }

    public String getZodSchema() {
        return zodSchema;
    }

    public String getAuthorId() {
        return authorId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<FormFieldMetadataDTO> getFormFields() {
        return formFields;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setVueTemplate(String vueTemplate) {
        this.vueTemplate = vueTemplate;
    }

    public void setZodSchema(String zodSchema) {
        this.zodSchema = zodSchema;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setFormFields(List<FormFieldMetadataDTO> formFields) {
        this.formFields = formFields;
    }
}
