package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.FormDefinition;
import java.util.Optional;
import java.util.UUID;

/**
 * Port out for Form Definition persistence operations.
 * Part of Hexagonal Architecture refactoring (ARQ-028-01).
 */
public interface FormDefinitionPort {

    boolean existsById(UUID id);

    Optional<FormDefinition> findById(UUID id);
    
    java.util.List<FormDefinition> findByFormIdOrderByVersionIdDesc(UUID formId);

    FormDefinition save(FormDefinition formDefinition);

}
