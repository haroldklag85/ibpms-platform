package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_sec_role_template")
public class RoleTemplateEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String roleName;

    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "JSON")
    private String granularPermissions; // Lista de permisos básicos ej: ["TASK:READ", "BPMN:DEPLOY"]

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGranularPermissions() {
        return granularPermissions;
    }

    public void setGranularPermissions(String granularPermissions) {
        this.granularPermissions = granularPermissions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
