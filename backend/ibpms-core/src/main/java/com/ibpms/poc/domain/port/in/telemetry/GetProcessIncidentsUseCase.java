package com.ibpms.poc.domain.port.in.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessIncidentMetric;
import java.util.List;

public interface GetProcessIncidentsUseCase {
    List<ProcessIncidentMetric> getProcessIncidents(int firstResult, int maxResults);
}
