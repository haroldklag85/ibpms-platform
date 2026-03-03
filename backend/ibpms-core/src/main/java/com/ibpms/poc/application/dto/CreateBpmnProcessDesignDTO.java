package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateBpmnProcessDesignDTO {

    @NotBlank(message = "El nombre del proceso es obligatorio.")
    private String name;

    @NotNull(message = "El patrón de formulario es obligatorio (SIMPLE o IFORM_MAESTRO).")
    private String formPattern;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormPattern() {
        return formPattern;
    }

    public void setFormPattern(String formPattern) {
        this.formPattern = formPattern;
    }
}
