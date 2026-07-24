// @Traceability: US-005/US-036 - ADR-011
package com.ibpms.poc.application.dto;

import java.util.UUID;

public record LaneRoleAssignmentDTO(
    UUID laneId,
    String laneName,
    String processKey,
    boolean canInitiate,
    boolean canExecute
) {}
