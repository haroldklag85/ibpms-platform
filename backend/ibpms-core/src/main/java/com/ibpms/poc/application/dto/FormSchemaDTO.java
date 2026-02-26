package com.ibpms.poc.application.dto;

import java.util.List;

public class FormSchemaDTO {
    private String formId;
    private String title;
    private String version;
    private String layout;
    private List<FormComponentDTO> components;
    private List<FormActionDTO> actions;

    // Getters y Setters
    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public List<FormComponentDTO> getComponents() {
        return components;
    }

    public void setComponents(List<FormComponentDTO> components) {
        this.components = components;
    }

    public List<FormActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<FormActionDTO> actions) {
        this.actions = actions;
    }
}
