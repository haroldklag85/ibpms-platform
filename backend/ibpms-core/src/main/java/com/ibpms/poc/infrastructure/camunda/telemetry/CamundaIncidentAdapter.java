package com.ibpms.poc.infrastructure.camunda.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessIncidentMetric;
import com.ibpms.poc.domain.port.out.telemetry.ProcessIncidentPort;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Incident;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CamundaIncidentAdapter implements ProcessIncidentPort {

    private final RuntimeService runtimeService;

    @Override
    public List<ProcessIncidentMetric> getProcessIncidents(int firstResult, int maxResults) {
        return runtimeService.createIncidentQuery()
                .orderByIncidentTimestamp().desc()
                .listPage(firstResult, maxResults)
                .stream()
                .map(this::mapToMetric)
                .collect(Collectors.toList());
    }

    private ProcessIncidentMetric mapToMetric(Incident incident) {
        return new ProcessIncidentMetric(
                incident.getId(),
                incident.getProcessInstanceId(),
                incident.getIncidentType(),
                incident.getIncidentMessage(),
                toLocalDateTime(incident.getIncidentTimestamp())
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
