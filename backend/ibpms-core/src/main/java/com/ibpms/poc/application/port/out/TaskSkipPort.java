package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.time.LocalDateTime;

/**
 * Puerto de salida para el registro y conteo de omisiones de tareas (Skip).
 */
@Traceability(US = "US-001", CA = {"CA-21"})
public interface TaskSkipPort {
    void save(TaskSkipEntity skipEntity);
    int countRecentSkips(String tenantId, String userId, LocalDateTime since);
}
