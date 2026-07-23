package com.ibpms.poc.domain.port.in.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessInstanceMetric;
import java.util.List;

public interface GetProcessInstancesUseCase {
    List<ProcessInstanceMetric> getProcessInstances(String state, int firstResult, int maxResults);
}
