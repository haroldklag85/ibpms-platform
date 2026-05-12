package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.WorkdeskProjectionPort;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
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
