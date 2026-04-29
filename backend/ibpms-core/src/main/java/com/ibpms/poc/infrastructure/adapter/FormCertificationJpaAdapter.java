package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.FormCertificationPort;
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
    public Optional<FormCertificationEntity> findByFormDefinitionId(UUID formDefinitionId) {
        return repository.findByFormDefinitionId(formDefinitionId);
    }

    @Override
    public FormCertificationEntity save(FormCertificationEntity entity) {
        return repository.save(entity);
    }
}
