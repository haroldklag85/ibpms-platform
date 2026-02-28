package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inbound_webhooks")
public class InboundWebhookEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "target_bpmn_process_key", nullable = false, length = 100)
    private String targetBpmnProcessKey;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "security_token", length = 255)
    private String securityToken;

    public InboundWebhookEntity() {
        this.id = UUID.randomUUID();
        this.isActive = true;
    }

    // Getters and Setters
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

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetBpmnProcessKey() {
        return targetBpmnProcessKey;
    }

    public void setTargetBpmnProcessKey(String targetBpmnProcessKey) {
        this.targetBpmnProcessKey = targetBpmnProcessKey;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}
