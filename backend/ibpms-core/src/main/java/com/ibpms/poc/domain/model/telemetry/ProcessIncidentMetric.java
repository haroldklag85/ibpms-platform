package com.ibpms.poc.domain.model.telemetry;

import java.time.LocalDateTime;

public record ProcessIncidentMetric(
        String id,
        String processInstanceId,
        String incidentType,
        String incidentMessage,
        LocalDateTime createTime
) {
}
