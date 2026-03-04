package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_sec_identity", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "entraid_object_id" })
})
@SQLDelete(sql = "UPDATE ibpms_sec_identity SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true") // Hibernate Filter para solo traer activos por defecto
public class IdentityEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "entraid_object_id", nullable = false, length = 100)
    private String entraIdObjectId; // UUID del usuario en Azure AD

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 150)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleTemplateEntity primaryRole;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEntraIdObjectId() {
        return entraIdObjectId;
    }

    public void setEntraIdObjectId(String entraIdObjectId) {
        this.entraIdObjectId = entraIdObjectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public RoleTemplateEntity getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(RoleTemplateEntity primaryRole) {
        this.primaryRole = primaryRole;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
