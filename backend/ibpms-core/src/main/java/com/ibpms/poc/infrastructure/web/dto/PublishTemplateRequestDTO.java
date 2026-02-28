package com.ibpms.poc.infrastructure.web.dto;

import com.ibpms.poc.domain.model.TemplateType;

public class PublishTemplateRequestDTO {

    private String name;
    private TemplateType type;
    private String rawCode;

    // Getters
    public String getName() {
        return name;
    }

    public TemplateType getType() {
        return type;
    }

    public String getRawCode() {
        return rawCode;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public void setRawCode(String rawCode) {
        this.rawCode = rawCode;
    }
}
