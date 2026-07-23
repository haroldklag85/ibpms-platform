package com.ibpms.poc.domain.port.out.telemetry;

import com.ibpms.poc.domain.model.telemetry.ProcessIncidentMetric;
import java.util.List;

public interface ProcessIncidentPort {
    List<ProcessIncidentMetric> getProcessIncidents(int firstResult, int maxResults);
}
