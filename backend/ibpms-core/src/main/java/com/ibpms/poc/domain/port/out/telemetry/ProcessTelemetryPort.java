package com.ibpms.poc.domain.port.out.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessInstanceMetric;
import java.util.List;

public interface ProcessTelemetryPort {
    List<ProcessInstanceMetric> getProcessInstances(String state, int firstResult, int maxResults);
}
