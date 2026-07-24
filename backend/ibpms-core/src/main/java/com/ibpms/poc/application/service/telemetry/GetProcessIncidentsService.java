package com.ibpms.poc.application.service.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessIncidentMetric;
import com.ibpms.poc.domain.port.in.telemetry.GetProcessIncidentsUseCase;
import com.ibpms.poc.domain.port.out.telemetry.ProcessIncidentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProcessIncidentsService implements GetProcessIncidentsUseCase {

    private final ProcessIncidentPort processIncidentPort;

    @Override
    public List<ProcessIncidentMetric> getProcessIncidents(int firstResult, int maxResults) {
        log.info("Fetching process incidents telemetry with offset: {}, max: {}", firstResult, maxResults);
        return processIncidentPort.getProcessIncidents(firstResult, maxResults);
    }
}
