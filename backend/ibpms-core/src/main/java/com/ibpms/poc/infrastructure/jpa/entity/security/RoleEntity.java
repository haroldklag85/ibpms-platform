package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "ibpms_security_role")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ibpms_security_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionEntity> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProcessPermissionEntity> processPermissions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id")
    private RoleEntity parentRole;

    // CA-1 US-036: Modelo híbrido EntraID/LOCAL — indica si este rol actúa como Plantilla Clonable
    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate = false;

    // CA-1 US-036: Origen del rol — "LOCAL" (creado en iBPMS) o "ENTRA_ID" (sincronizado desde Azure AD)
    @Column(name = "source", length = 50, nullable = false)
    private String source = "LOCAL";

    @Column(name = "is_vip_restricted")
    private Boolean isVipRestricted = false;

    @Column(name = "process_definition_id", length = 255)
    private String processDefinitionId;

    @Column(name = "lane_id", length = 255)
    private String laneId;

    public RoleEntity() {}

    public RoleEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<UserEntity> getUsers() { return users; }
    public void setUsers(Set<UserEntity> users) { this.users = users; }
    public Set<PermissionEntity> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionEntity> permissions) { this.permissions = permissions; }
    public Set<ProcessPermissionEntity> getProcessPermissions() { return processPermissions; }
    public void setProcessPermissions(Set<ProcessPermissionEntity> processPermissions) { this.processPermissions = processPermissions; }
    public RoleEntity getParentRole() { return parentRole; }
    public void setParentRole(RoleEntity parentRole) { this.parentRole = parentRole; }
    public Boolean getIsTemplate() { return isTemplate; }
    public void setIsTemplate(Boolean isTemplate) { this.isTemplate = isTemplate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Boolean getIsVipRestricted() { return isVipRestricted; }
    public void setIsVipRestricted(Boolean isVipRestricted) { this.isVipRestricted = isVipRestricted; }
    public String getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }
    public String getLaneId() { return laneId; }
    public void setLaneId(String laneId) { this.laneId = laneId; }
}
