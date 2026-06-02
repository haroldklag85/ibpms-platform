// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.FormCertificationPort;
import com.ibpms.poc.domain.model.FormCertification;
import com.ibpms.poc.infrastructure.jpa.entity.FormCertificationEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormCertificationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FormCertificationJpaAdapter implements FormCertificationPort {

    private final FormCertificationRepository repository;

    public FormCertificationJpaAdapter(FormCertificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FormCertification> findByFormDefinitionId(UUID formDefinitionId) {
        return repository.findByFormDefinitionId(formDefinitionId)
                .map(this::toDomain);
    }

    @Override
    public FormCertification save(FormCertification domain) {
        FormCertificationEntity entity = toEntity(domain);
        FormCertificationEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private FormCertification toDomain(FormCertificationEntity entity) {
        if (entity == null) return null;
        return new FormCertification(
                entity.getId(),
                entity.getFormDefinitionId(),
                entity.getIsQaCertified(),
                entity.getCertifiedSchemaHash(),
                entity.getCertifiedBy(),
                entity.getCertifiedAt()
        );
    }

    private FormCertificationEntity toEntity(FormCertification domain) {
        if (domain == null) return null;
        FormCertificationEntity entity = new FormCertificationEntity();
        entity.setId(domain.getId());
        entity.setFormDefinitionId(domain.getFormDefinitionId());
        entity.setIsQaCertified(domain.getIsQaCertified());
        entity.setCertifiedSchemaHash(domain.getCertifiedSchemaHash());
        entity.setCertifiedBy(domain.getCertifiedBy());
        entity.setCertifiedAt(domain.getCertifiedAt());
        return entity;
    }
}
