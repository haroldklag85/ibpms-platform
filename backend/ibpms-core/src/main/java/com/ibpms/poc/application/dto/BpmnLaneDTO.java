// @Traceability: US-005/US-036 - ADR-011
package com.ibpms.poc.application.dto;

import java.util.UUID;

public record BpmnLaneDTO(
    UUID id,
    String processKey,
    String laneXmlId,
    String laneName,
    String actorDescription,
    String linkedRoleName
) {}
