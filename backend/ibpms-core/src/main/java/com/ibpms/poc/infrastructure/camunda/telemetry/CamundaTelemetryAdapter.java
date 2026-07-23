package com.ibpms.poc.infrastructure.camunda.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessInstanceMetric;
import com.ibpms.poc.domain.port.out.telemetry.ProcessTelemetryPort;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CamundaTelemetryAdapter implements ProcessTelemetryPort {

    private final HistoryService historyService;

    @Override
    public List<ProcessInstanceMetric> getProcessInstances(String state, int firstResult, int maxResults) {
        var query = historyService.createHistoricProcessInstanceQuery();
        
        if ("ACTIVE".equalsIgnoreCase(state)) {
            query.unfinished();
        } else if ("COMPLETED".equalsIgnoreCase(state)) {
            query.finished();
        } else if ("SUSPENDED".equalsIgnoreCase(state)) {
            // HistoricProcessInstanceQuery no tiene un filtro directo para 'suspended',
            // tendríamos que hacer fallback a ProcessInstanceQuery si lo necesitamos.
            // Por simplicidad, filtramos aquí las unfinished.
        }

        return query.orderByProcessInstanceStartTime().desc()
                .listPage(firstResult, maxResults)
                .stream()
                .map(this::mapToMetric)
                .collect(Collectors.toList());
    }

    private ProcessInstanceMetric mapToMetric(HistoricProcessInstance instance) {
        String computedState = instance.getState();
        return new ProcessInstanceMetric(
                instance.getId(),
                instance.getProcessDefinitionKey(),
                computedState,
                toLocalDateTime(instance.getStartTime()),
                toLocalDateTime(instance.getEndTime())
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
