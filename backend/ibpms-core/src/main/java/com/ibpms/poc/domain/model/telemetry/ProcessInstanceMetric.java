package com.ibpms.poc.domain.model.telemetry;

import java.time.LocalDateTime;

public record ProcessInstanceMetric(
        String id,
        String processDefinitionKey,
        String state,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
