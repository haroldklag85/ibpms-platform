package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_security_audit_log")
public class SecurityAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "is_break_glass")
    private Boolean isBreakGlass = false;

    @Lob
    @Column(name = "justification")
    private String justification;

    public SecurityAuditLogEntity() {
        this.timestamp = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public Boolean getIsBreakGlass() { return isBreakGlass; }
    public void setIsBreakGlass(Boolean isBreakGlass) { this.isBreakGlass = isBreakGlass; }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
