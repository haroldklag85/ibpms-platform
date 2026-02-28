package com.ibpms.poc.infrastructure.web.dto;

import com.ibpms.poc.domain.model.TemplateType;
import com.ibpms.poc.domain.model.UiTemplate;

import java.time.LocalDateTime;

public class UiTemplateDTO {

    private String id;
    private String name;
    private TemplateType type;
    private String version;
    private LocalDateTime createdAt;
    // No incluimos rawCode masivamente en listas para ahorrar BW. Se pedirá por el
    // endpoint /raw

    public UiTemplateDTO(String id, String name, TemplateType type, String version, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.version = version;
        this.createdAt = createdAt;
    }

    public static UiTemplateDTO fromDomain(UiTemplate template) {
        return new UiTemplateDTO(
                template.getId(),
                template.getName(),
                template.getType(),
                template.getVersion(),
                template.getCreatedAt());
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemplateType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
