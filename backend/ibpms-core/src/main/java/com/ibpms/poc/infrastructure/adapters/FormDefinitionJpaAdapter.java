package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.FormDefinitionPort;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para FormDefinitionPort.
 * @Traceability: US-029 - OBS-QA-01 - Desacoplar la validación de JSON Schema real
 */
@Component
public class FormDefinitionJpaAdapter implements FormDefinitionPort {

    private final FormDefinitionRepository formDefinitionRepository;

    public FormDefinitionJpaAdapter(FormDefinitionRepository formDefinitionRepository) {
        this.formDefinitionRepository = formDefinitionRepository;
    }

    @Override
    public Optional<String> findSchemaContentByVersion(String schemaVersion) {
        try {
            UUID id = UUID.fromString(schemaVersion);
            return formDefinitionRepository.findById(id).map(FormDefinitionEntity::getSchemaContent);
        } catch (IllegalArgumentException e) {
            // Manejar si schemaVersion es del tipo "formId:versionId" o similar si fuera necesario.
            // Actualmente se asume que se envía el UUID directo si proviene de ibpms_form_definitions
            return Optional.empty();
        }
    }
}
