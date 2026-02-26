package com.ibpms.poc.application.dto;

import java.util.List;
import java.util.Map;

public class FormComponentDTO {
    private String id;
    private String type; // text, textarea, select, catalog_select, date, number, checkbox
    private String label;
    private String placeholder;
    private Boolean required;
    private Boolean readonly;
    private Boolean hidden;
    private Integer rows;
    private Object defaultValue;
    private String catalogSource;
    private String dependentOn;
    private Map<String, Object> validation;
    private List<OptionDTO> options;

    public static class OptionDTO {
        private String value;
        private String label;

        public OptionDTO() {
        }

        public OptionDTO(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

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

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getCatalogSource() {
        return catalogSource;
    }

    public void setCatalogSource(String catalogSource) {
        this.catalogSource = catalogSource;
    }

    public String getDependentOn() {
        return dependentOn;
    }

    public void setDependentOn(String dependentOn) {
        this.dependentOn = dependentOn;
    }

    public Map<String, Object> getValidation() {
        return validation;
    }

    public void setValidation(Map<String, Object> validation) {
        this.validation = validation;
    }

    public List<OptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDTO> options) {
        this.options = options;
    }
}
