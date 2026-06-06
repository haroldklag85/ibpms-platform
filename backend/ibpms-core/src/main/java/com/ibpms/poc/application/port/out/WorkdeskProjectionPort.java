package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Optional;

/**
 * Puerto de salida para el acceso a las proyecciones de la bandeja de entrada (Workdesk).
 * Desacopla la lógica de negocio de la implementación JPA.
 */
@Traceability(US = "US-001", CA = {"CA-28", "CA-21"})
public interface WorkdeskProjectionPort {
    Optional<WorkdeskProjectionEntity> findNextAvailableTask(String tenantId, String[] skills);
    Optional<WorkdeskProjectionEntity> findById(String id);
    void save(WorkdeskProjectionEntity entity);
}
