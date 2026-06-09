// @Traceability: US-003 - ADR-001
package com.ibpms.poc.domain.model.agile;

import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileTask {
    private UUID id;
    private UUID projectId;
    private String teamId;
    private String tenantId;
    private String title;
    private String description;
    private BigDecimal effortEstimated;
    private BigDecimal effortActual;
    private String notes;
    private String status;
    private Integer position;
    private String draftPayload;
    private String draftPayloadHash;
    private ZonedDateTime draftExpiresAt;
    private ZonedDateTime slaDeadline;
    private ZonedDateTime lastActivityAt;
    @Builder.Default
    private Integer timeoutExtensions = 0;
    private String createdBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Set<String> assigneeIds;
    private Set<String> tags;
}
