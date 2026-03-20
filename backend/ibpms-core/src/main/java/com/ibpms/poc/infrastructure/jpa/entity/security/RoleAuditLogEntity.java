package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_security_role_audit_log")
public class RoleAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "admin_id", nullable = false, length = 100)
    private String adminId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Lob
    @Column(name = "delta_json", nullable = false)
    private String deltaJson;

    public RoleAuditLogEntity() {}

    public RoleAuditLogEntity(UUID roleId, String adminId, LocalDateTime timestamp, String action, String deltaJson) {
        this.roleId = roleId;
        this.adminId = adminId;
        this.timestamp = timestamp;
        this.action = action;
        this.deltaJson = deltaJson;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDeltaJson() { return deltaJson; }
    public void setDeltaJson(String deltaJson) { this.deltaJson = deltaJson; }
}
