// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.dto.FormFieldMetadataDTO;
import com.ibpms.poc.application.port.out.FormDesignPort;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para FormDesignPort.
 * Responsable de traducir las operaciones de negocio de formularios hacia
 * la base de datos subyacente. Aísla la capa de dominio del conocimiento
 * sobre entidades (FormDesignEntity), Hibernate y serialización JSON nativa.
 * 
 * Requerimiento: US-003 (iForm Maestro - Formularios Dinámicos)
 * Implementa soporte a nivel infraestructura para: CA-01, CA-11, CA-21, CA-26.
 * 
 * Implementa @RequiredArgsConstructor indirectamente o por inyección explícita limpia.
 */
@Component
public class FormDesignJpaAdapter implements FormDesignPort {

    private final FormDesignRepository formDesignRepository;
    private final ObjectMapper objectMapper;

    public FormDesignJpaAdapter(FormDesignRepository formDesignRepository, ObjectMapper objectMapper) {
        this.formDesignRepository = formDesignRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<FormDesignDTO> findAllActive() {
        return formDesignRepository.findAll().stream()
                .filter(f -> f.getStatus() != FormDesignEntity.Status.DELETED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FormDesignDTO> findByTechnicalNameAndVersion(String technicalName, Integer version) {
        return formDesignRepository.findByTechnicalNameAndVersion(technicalName, version).map(this::toDto);
    }

    @Override
    public Optional<FormDesignDTO> findById(UUID id) {
        return formDesignRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<FormDesignDTO> findAllByTechnicalName(String technicalName) {
        return formDesignRepository.findAll().stream()
                .filter(f -> f.getTechnicalName().equals(technicalName))
                .sorted(java.util.Comparator.comparing(FormDesignEntity::getVersion).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FormDesignDTO> findTopByTechnicalNameOrderByVersionDesc(String technicalName) {
        return formDesignRepository.findTopByTechnicalNameOrderByVersionDesc(technicalName).map(this::toDto);
    }

    @Override
    public FormDesignDTO createNew(FormDesignDTO dto) {
        FormDesignEntity entity = new FormDesignEntity();
        entity.setName(dto.getName());
        entity.setTechnicalName(dto.getTechnicalName());
        entity.setPattern(FormDesignEntity.Pattern.valueOf(dto.getPattern()));
        entity.setVueTemplate(dto.getVueTemplate());
        entity.setZodSchema(dto.getZodSchema());
        try {
            entity.setFormFields(dto.getFormFields() != null ? objectMapper.writeValueAsString(dto.getFormFields()) : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando formFields", e);
        }
        entity.setAuthorId(dto.getAuthorId());
        
        formDesignRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public FormDesignDTO saveVersion(FormDesignDTO dto) {
        FormDesignEntity entity;
        if (dto.getId() != null) {
            entity = formDesignRepository.findById(dto.getId()).orElse(new FormDesignEntity());
        } else {
            entity = new FormDesignEntity();
            entity.setPattern(FormDesignEntity.Pattern.valueOf(dto.getPattern()));
            entity.setTechnicalName(dto.getTechnicalName());
        }

        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-87: Asegurar persistencia del version_id incrementado
        entity.setVersion(dto.getVersion());
        entity.setName(dto.getName());
        entity.setVueTemplate(dto.getVueTemplate());
        entity.setZodSchema(dto.getZodSchema());
        try {
            entity.setFormFields(dto.getFormFields() != null ? objectMapper.writeValueAsString(dto.getFormFields()) : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando formFields", e);
        }
        entity.setAuthorId(dto.getAuthorId());
        entity.setUpdatedAt(LocalDateTime.now());

        formDesignRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public void updateStatusToDeleted(UUID id) {
        formDesignRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(FormDesignEntity.Status.DELETED);
            entity.setUpdatedAt(LocalDateTime.now());
            formDesignRepository.save(entity);
        });
    }

    private FormDesignDTO toDto(FormDesignEntity e) {
        FormDesignDTO dto = new FormDesignDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setTechnicalName(e.getTechnicalName());
        dto.setPattern(e.getPattern() != null ? e.getPattern().name() : null);
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        dto.setVersion(e.getVersion());
        dto.setVueTemplate(e.getVueTemplate());
        dto.setZodSchema(e.getZodSchema());
        if (e.getFormFields() != null) {
            try {
                dto.setFormFields((List<Map<String, Object>>) objectMapper.readValue(e.getFormFields(), List.class));
            } catch (JsonProcessingException ex) {
                dto.setFormFields(Collections.emptyList());
            }
        }
        dto.setAuthorId(e.getAuthorId());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
