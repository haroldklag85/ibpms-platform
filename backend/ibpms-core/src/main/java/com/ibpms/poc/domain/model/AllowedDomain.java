// @Traceability: US-003 - ADR-001
package com.ibpms.poc.domain.model;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Whitelist entry for authorized webhook sender domains (US-004 CA-4, CA-12).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllowedDomain {
    private UUID id;
    private String domain;
    private String tenantId;
    private String description;
    private String createdBy;
    private ZonedDateTime createdAt;
    private Boolean isActive;
}
