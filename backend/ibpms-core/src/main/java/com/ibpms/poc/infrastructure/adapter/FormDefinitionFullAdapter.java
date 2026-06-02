// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.domain.model.FormDefinition;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter JPA para la interfaz FormDefinitionPort (paquete application.port.out — singular).
 * Implementa las operaciones CRUD completas requeridas por FormCertificationService
 * y FormDefinitionController.
 *
 * @Traceability: US-003, US-029 — CA-12, CA-13, CA-15, CA-16
 *                Cierre de bloqueante P0: FormDefinitionPort package split (Sprint J-04 Iter.2)
 */
@Component("formDefinitionFullAdapter")
public class FormDefinitionFullAdapter implements FormDefinitionPort {

    private final FormDefinitionRepository repository;

    public FormDefinitionFullAdapter(FormDefinitionRepository repository) {
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
    public List<FormDefinition> findByFormIdOrderByVersionIdDesc(UUID formId) {
        return repository.findByFormIdOrderByVersionIdDesc(formId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public FormDefinition save(FormDefinition domain) {
        FormDefinitionEntity entity = toEntity(domain);
        FormDefinitionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<String> findSchemaContentByVersion(String schemaVersion) {
        try {
            UUID id = UUID.fromString(schemaVersion);
            return repository.findById(id).map(FormDefinitionEntity::getSchemaContent);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // ─────────────────── Mappers Domain ↔ Entity ───────────────────

    private FormDefinition toDomain(FormDefinitionEntity e) {
        return new FormDefinition(
                e.getId(),
                e.getFormId(),
                e.getVersionId(),
                e.getSchemaContent(),
                e.getHashSha256(),
                e.getCreatedBy(),
                e.getCreatedAt()
        );
    }

    private FormDefinitionEntity toEntity(FormDefinition d) {
        FormDefinitionEntity e = new FormDefinitionEntity();
        if (d.getId() != null) {
            e.setId(d.getId());
        }
        e.setFormId(d.getFormId());
        e.setVersionId(d.getVersionId());
        e.setSchemaContent(d.getSchemaContent());
        e.setCreatedBy(d.getCreatedBy());
        e.setHashSha256(d.getHashSha256());
        if (d.getCreatedAt() != null) {
            e.setCreatedAt(d.getCreatedAt());
        }
        return e;
    }
}
