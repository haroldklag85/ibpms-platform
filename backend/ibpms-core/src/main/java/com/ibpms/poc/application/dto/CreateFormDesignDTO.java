package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateFormDesignDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "El nombre técnico es obligatorio.")
    private String technicalName;

    @NotNull(message = "El patrón (SIMPLE / IFORM_MAESTRO) es obligatorio.")
    private String pattern;

    private String vueTemplate;
    private String zodSchema;
    private List<FormFieldMetadataDTO> formFields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getVueTemplate() {
        return vueTemplate;
    }

    public void setVueTemplate(String vueTemplate) {
        this.vueTemplate = vueTemplate;
    }

    public String getZodSchema() {
        return zodSchema;
    }

    public void setZodSchema(String zodSchema) {
        this.zodSchema = zodSchema;
    }

    public List<FormFieldMetadataDTO> getFormFields() {
        return formFields;
    }

    public void setFormFields(List<FormFieldMetadataDTO> formFields) {
        this.formFields = formFields;
    }
}
