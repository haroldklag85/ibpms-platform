package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.domain.model.FormDefinition;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
    public Optional<FormDefinition> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public java.util.List<FormDefinition> findByFormIdOrderByVersionIdDesc(UUID formId) {
        return repository.findByFormIdOrderByVersionIdDesc(formId).stream()
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public FormDefinition save(FormDefinition domain) {
        FormDefinitionEntity entity = toEntity(domain);
        FormDefinitionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private FormDefinition toDomain(FormDefinitionEntity entity) {
        if (entity == null) return null;
        return new FormDefinition(
                entity.getId(),
                entity.getFormId(),
                entity.getVersionId(),
                entity.getSchemaContent(),
                entity.getHashSha256(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    private FormDefinitionEntity toEntity(FormDefinition domain) {
        if (domain == null) return null;
        FormDefinitionEntity entity = new FormDefinitionEntity();
        entity.setId(domain.getId());
        entity.setFormId(domain.getFormId());
        entity.setVersionId(domain.getVersionId());
        entity.setSchemaContent(domain.getSchemaContent());
        entity.setHashSha256(domain.getHashSha256());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
