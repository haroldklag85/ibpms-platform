package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_delegation")
@Getter
@Setter
public class UserDelegationEntity {

    @Id
    @Column(columnDefinition = "bpchar")
    private UUID id;

    @Column(name = "supervisor_id", nullable = false, length = 100)
    private String supervisorId;

    @Column(name = "assistant_id", nullable = false, length = 100)
    private String assistantId;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserDelegationEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
}
