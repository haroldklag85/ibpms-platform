package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import java.util.Optional;
import java.util.UUID;

/**
 * Port out for Form Definition persistence operations.
 * Part of Hexagonal Architecture refactoring (ARQ-028-01).
 */
public interface FormDefinitionPort {

    boolean existsById(UUID id);

    Optional<FormDefinitionEntity> findById(UUID id);

    FormDefinitionEntity save(FormDefinitionEntity entity);

}
