// @Traceability: US-003 - ADR-001
package com.ibpms.poc.domain.model.agile;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileSlaChangelog {
    private UUID id;
    private UUID taskId;
    private ZonedDateTime previousValue;
    private ZonedDateTime newValue;
    private String changedBy;
    private ZonedDateTime changedAt;
}
