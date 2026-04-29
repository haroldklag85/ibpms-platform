package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.FormCertificationEntity;
import java.util.Optional;
import java.util.UUID;

public interface FormCertificationPort {
    Optional<FormCertificationEntity> findByFormDefinitionId(UUID formDefinitionId);
    FormCertificationEntity save(FormCertificationEntity entity);
}
