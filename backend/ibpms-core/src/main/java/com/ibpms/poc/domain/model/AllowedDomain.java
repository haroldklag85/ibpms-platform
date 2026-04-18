package com.ibpms.poc.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Whitelist entry for authorized webhook sender domains (US-004 CA-4, CA-12).
 */
@Entity
@Table(name = "ibpms_webhook_allowed_domains")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllowedDomain {

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
