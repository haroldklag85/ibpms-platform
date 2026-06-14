package com.ibpms.poc.infrastructure.jpa.entity;

import com.ibpms.poc.domain.model.agile.SkipReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_skip_audit")
@Getter
@Setter
public class SkipAuditEntity {

    @Id
    @Column(columnDefinition = "bpchar")
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "skipped_by", nullable = false, length = 100)
    private String skippedBy;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkipReason reason;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "skipped_at", nullable = false, updatable = false)
    private LocalDateTime skippedAt;

    public SkipAuditEntity() {
        this.id = UUID.randomUUID();
        this.skippedAt = LocalDateTime.now();
    }
}
