package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.UiTemplatePersistencePort;
import com.ibpms.poc.domain.model.UiTemplate;
import com.ibpms.poc.infrastructure.jpa.entity.UiTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.UiTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UiTemplatePersistenceAdapter implements UiTemplatePersistencePort {

    private final UiTemplateRepository repository;

    public UiTemplatePersistenceAdapter(UiTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public UiTemplate save(UiTemplate template) {
        UiTemplateEntity entity = new UiTemplateEntity(
                template.getId(),
                template.getName(),
                template.getType(),
                template.getRawCode(),
                template.getVersion(),
                template.getCreatedAt());
        UiTemplateEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<UiTemplate> findLatestByName(String name) {
        return repository.findFirstByNameOrderByCreatedAtDesc(name)
                .map(this::mapToDomain);
    }

    @Override
    public List<UiTemplate> findAllVersionsByName(String name) {
        return repository.findAllByNameOrderByCreatedAtDesc(name).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndVersion(String name, String version) {
        return repository.existsByNameAndVersion(name, version);
    }

    private UiTemplate mapToDomain(UiTemplateEntity entity) {
        return UiTemplate.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .rawCode(entity.getRawCode())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
