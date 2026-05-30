// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.WorkdeskProjectionPort;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador JPA para el puerto de proyecciones del Workdesk.
 * Provee la implementación física a la interfaz definida por el dominio.
 */
@Component
@Traceability(US = "US-001", CA = {"CA-28", "CA-21"})
public class WorkdeskProjectionJpaAdapter implements WorkdeskProjectionPort {

    private final WorkdeskProjectionRepository repository;

    public WorkdeskProjectionJpaAdapter(WorkdeskProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<WorkdeskProjectionEntity> findNextAvailableTask(String tenantId, String[] skills) {
        return repository.findNextAvailableTask(tenantId, skills);
    }

    @Override
    public Optional<WorkdeskProjectionEntity> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public void save(WorkdeskProjectionEntity entity) {
        repository.save(entity);
    }
}
