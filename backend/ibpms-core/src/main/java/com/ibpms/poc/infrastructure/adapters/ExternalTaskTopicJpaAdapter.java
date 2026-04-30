package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.ExternalTaskTopicPort;
import com.ibpms.poc.domain.model.ExternalTaskTopic;
import com.ibpms.poc.infrastructure.jpa.entity.ExternalTaskTopicEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ExternalTaskTopicRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExternalTaskTopicJpaAdapter implements ExternalTaskTopicPort {

    private final ExternalTaskTopicRepository repository;

    public ExternalTaskTopicJpaAdapter(ExternalTaskTopicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ExternalTaskTopic> findByIsActiveTrue() {
        return repository.findByIsActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExternalTaskTopic> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ExternalTaskTopic toDomain(ExternalTaskTopicEntity entity) {
        ExternalTaskTopic domain = new ExternalTaskTopic();
        domain.setTopicName(entity.getTopicName());
        domain.setDescription(entity.getDescription());
        domain.setWorkerClass(entity.getWorkerClass());
        domain.setIsActive(entity.isActive());
        domain.setRegisteredAt(entity.getRegisteredAt());
        return domain;
    }
}
