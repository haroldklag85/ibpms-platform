package com.ibpms.poc.infrastructure.jpa.entity.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enterprise Prompt Library (CA-7, CA-23).
 */
@Entity
@Table(name = "ibpms_prompt_template")
public class PromptTemplateEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "template_string", columnDefinition = "TEXT", nullable = false)
    private String templateString;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PromptTemplateEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.version = 1;
    }

    public PromptTemplateEntity(String name, String templateString) {
        this();
        this.name = name;
        this.templateString = templateString;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTemplateString() {
        return templateString;
    }

    public Integer getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTemplateString(String templateString) {
        this.templateString = templateString;
        this.version++;
    }
}
