package com.ibpms.poc.application.dto;

public class FormActionDTO {
    private String id;
    private String type; // submit, cancel, etc.
    private String label;
    private String theme; // primary, secondary, danger, etc.

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
