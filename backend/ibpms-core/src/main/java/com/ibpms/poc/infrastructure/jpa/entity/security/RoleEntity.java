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
}
