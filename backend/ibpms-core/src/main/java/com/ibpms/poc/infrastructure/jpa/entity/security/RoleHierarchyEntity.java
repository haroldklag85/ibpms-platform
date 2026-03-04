package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ibpms_sec_role_hierarchy")
public class RoleHierarchyEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", nullable = false)
    private RoleTemplateEntity parentRole; // Ej: Gerente_Riesgos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_role_id", nullable = false)
    private RoleTemplateEntity childRole; // Ej: Analista_Riesgos

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RoleTemplateEntity getParentRole() {
        return parentRole;
    }

    public void setParentRole(RoleTemplateEntity parentRole) {
        this.parentRole = parentRole;
    }

    public RoleTemplateEntity getChildRole() {
        return childRole;
    }

    public void setChildRole(RoleTemplateEntity childRole) {
        this.childRole = childRole;
    }
}
