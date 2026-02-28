package com.ibpms.poc.infrastructure.jpa.entity;

import com.ibpms.poc.domain.model.TemplateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_ui_template", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name", "version" })
})
public class UiTemplateEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TemplateType type;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawCode;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UiTemplateEntity() {
    }

    public UiTemplateEntity(String id, String name, TemplateType type, String rawCode, String version,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rawCode = rawCode;
        this.version = version;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemplateType getType() {
        return type;
    }

    public String getRawCode() {
        return rawCode;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
