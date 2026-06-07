package com.ibpms.poc.api.controller.telemetry.dto;

import java.time.LocalDateTime;

public record ProcessInstanceMetricDto(
        String id,
        String processDefinitionKey,
        String state,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
