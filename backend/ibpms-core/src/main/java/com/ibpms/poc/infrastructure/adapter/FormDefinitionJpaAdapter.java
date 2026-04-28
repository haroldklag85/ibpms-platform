package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for FormDefinitionPort using JPA repository.
 * Part of Hexagonal Architecture refactoring (ARQ-028-01).
 */
@Component
public class FormDefinitionJpaAdapter implements FormDefinitionPort {

    private final FormDefinitionRepository repository;

    public FormDefinitionJpaAdapter(FormDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<FormDefinitionEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public FormDefinitionEntity save(FormDefinitionEntity entity) {
        return repository.save(entity);
    }
}
