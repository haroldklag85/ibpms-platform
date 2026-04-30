package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.BpmnDesignPort;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BpmnDesignJpaAdapter implements BpmnDesignPort {

    private final BpmnProcessDesignRepository repository;

    public BpmnDesignJpaAdapter(BpmnProcessDesignRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BpmnProcessDesign> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<BpmnProcessDesign> findByTechnicalId(String technicalId) {
        return repository.findByTechnicalId(technicalId).map(this::toDomain);
    }

    @Override
    public BpmnProcessDesign save(BpmnProcessDesign design) {
        BpmnProcessDesignEntity entity = toEntity(design);
        BpmnProcessDesignEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private BpmnProcessDesign toDomain(BpmnProcessDesignEntity entity) {
        BpmnProcessDesign domain = BpmnProcessDesign.reconstituir(
                entity.getId(),
                entity.getName(),
                entity.getTechnicalId(),
                entity.getFormPattern() != null ? BpmnProcessDesign.FormPattern.valueOf(entity.getFormPattern().name()) : null,
                entity.getStatus() != null ? BpmnProcessDesign.Status.valueOf(entity.getStatus().name()) : BpmnProcessDesign.Status.DRAFT,
                entity.getCurrentVersion(),
                entity.getLockedBy(),
                entity.getLockedAt(),
                entity.getXmlDraft(),
                entity.getMaxNodes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy()
        );
        domain.updateGenericFormConfig(entity.getGenericFormWhitelist());
        return domain;
    }

    private BpmnProcessDesignEntity toEntity(BpmnProcessDesign domain) {
        BpmnProcessDesignEntity entity = new BpmnProcessDesignEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setTechnicalId(domain.getTechnicalId());
        entity.setFormPattern(domain.getFormPattern() != null ? BpmnProcessDesignEntity.FormPattern.valueOf(domain.getFormPattern().name()) : null);
        entity.setStatus(domain.getStatus() != null ? BpmnProcessDesignEntity.Status.valueOf(domain.getStatus().name()) : null);
        entity.setCurrentVersion(domain.getCurrentVersion());
        entity.setLockedBy(domain.getLockedBy());
        entity.setLockedAt(domain.getLockedAt());
        entity.setXmlDraft(domain.getXmlDraft());
        entity.setMaxNodes(domain.getMaxNodes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setGenericFormWhitelist(domain.getGenericFormWhitelist());
        // optLockVersion is managed by JPA
        return entity;
    }
}
