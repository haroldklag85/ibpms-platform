package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA - Diseño Inmutable de Formularios Vue3/Zod (Pantalla 7).
 */
@Entity
@Table(name = "ibpms_form_design")
public class FormDesignEntity {

    public enum Pattern {
        SIMPLE, IFORM_MAESTRO
    }

    public enum Status {
        DRAFT, ACTIVE, DELETED
    }

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "technical_name", nullable = false, length = 100)
    private String technicalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern", nullable = false, length = 20)
    private Pattern pattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "vue_template", columnDefinition = "LONGTEXT")
    private String vueTemplate;

    @Column(name = "zod_schema", columnDefinition = "LONGTEXT")
    private String zodSchema;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "author_id", nullable = false, length = 50)
    private String authorId;

    public FormDesignEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1;
        this.status = Status.DRAFT;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
