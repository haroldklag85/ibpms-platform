// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_webhook_allowed_domains")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllowedDomainJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "domain", nullable = false)
    private String domain;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
