package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.AgileTaskPort;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AgileTaskJpaAdapter implements AgileTaskPort {

    private final AgileTaskRepositoryJpa repository;

    public AgileTaskJpaAdapter(AgileTaskRepositoryJpa repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AgileTask> findByIdForUpdate(UUID taskId) {
        return repository.findByIdForUpdate(taskId);
    }

    @Override
    public AgileTask save(AgileTask task) {
        return repository.save(task);
    }
}
