package com.ibpms.poc.application.service.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessInstanceMetric;
import com.ibpms.poc.domain.port.in.telemetry.GetProcessInstancesUseCase;
import com.ibpms.poc.domain.port.out.telemetry.ProcessTelemetryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProcessInstancesService implements GetProcessInstancesUseCase {

    private final ProcessTelemetryPort processTelemetryPort;

    @Override
    public List<ProcessInstanceMetric> getProcessInstances(String state, int firstResult, int maxResults) {
        log.info("Fetching process instances telemetry with state: {}, offset: {}, max: {}", state, firstResult, maxResults);
        return processTelemetryPort.getProcessInstances(state, firstResult, maxResults);
    }
}
