package com.ibpms.poc.api.controller.telemetry.dto;

import java.time.LocalDateTime;

public record ProcessIncidentMetricDto(
        String id,
        String processInstanceId,
        String incidentType,
        String incidentMessage,
        LocalDateTime createTime
) {
}
