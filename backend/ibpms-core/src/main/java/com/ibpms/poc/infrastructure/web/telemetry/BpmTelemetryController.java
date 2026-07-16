package com.ibpms.poc.infrastructure.web.telemetry;

import com.ibpms.poc.api.controller.telemetry.dto.ProcessIncidentMetricDto;
import com.ibpms.poc.api.controller.telemetry.dto.ProcessInstanceMetricDto;
import com.ibpms.poc.domain.model.telemetry.ProcessIncidentMetric;
import com.ibpms.poc.domain.model.telemetry.ProcessInstanceMetric;
import com.ibpms.poc.domain.port.in.telemetry.GetProcessIncidentsUseCase;
import com.ibpms.poc.domain.port.in.telemetry.GetProcessInstancesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bpm/telemetry")
@RequiredArgsConstructor
public class BpmTelemetryController {

    private final GetProcessInstancesUseCase getProcessInstancesUseCase;
    private final GetProcessIncidentsUseCase getProcessIncidentsUseCase;

    @GetMapping("/instances")
    public ResponseEntity<List<ProcessInstanceMetricDto>> getInstances(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int firstResult,
            @RequestParam(defaultValue = "50") int maxResults) {
        
        List<ProcessInstanceMetricDto> response = getProcessInstancesUseCase.getProcessInstances(state, firstResult, maxResults)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<ProcessIncidentMetricDto>> getIncidents(
            @RequestParam(defaultValue = "0") int firstResult,
            @RequestParam(defaultValue = "50") int maxResults) {
            
        List<ProcessIncidentMetricDto> response = getProcessIncidentsUseCase.getProcessIncidents(firstResult, maxResults)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    private ProcessInstanceMetricDto mapToDto(ProcessInstanceMetric metric) {
        return new ProcessInstanceMetricDto(
                metric.id(),
                metric.processDefinitionKey(),
                metric.state(),
                metric.startTime(),
                metric.endTime()
        );
    }

    private ProcessIncidentMetricDto mapToDto(ProcessIncidentMetric metric) {
        return new ProcessIncidentMetricDto(
                metric.id(),
                metric.processInstanceId(),
                metric.incidentType(),
                metric.incidentMessage(),
                metric.createTime()
        );
    }
}
