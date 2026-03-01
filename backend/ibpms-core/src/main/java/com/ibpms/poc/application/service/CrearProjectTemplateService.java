package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.ProjectTemplateDTO;
import com.ibpms.poc.application.port.in.CrearProjectTemplateUseCase;
import com.ibpms.poc.domain.model.PhaseTemplate;
import com.ibpms.poc.domain.model.ProjectTemplate;
import com.ibpms.poc.infrastructure.jpa.entity.ProjectTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProjectTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CrearProjectTemplateService implements CrearProjectTemplateUseCase {

    private final ProjectTemplateRepository repository;
    private final ObjectMapper objectMapper;

    public CrearProjectTemplateService(ProjectTemplateRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProjectTemplateDTO crearPlantilla(ProjectTemplateDTO dto, String createdBy) {
        // 1. Instanciar Agregado de Dominio Principal
        ProjectTemplate domain = new ProjectTemplate(
                UUID.randomUUID(),
                dto.getName(),
                dto.getDescription(),
                dto.getCategory(),
                createdBy);

        // 2. Mapear Fases JSON
        if (dto.getPhases() != null && !dto.getPhases().isEmpty()) {
            dto.getPhases().forEach(phaseDto -> {
                PhaseTemplate phase = new PhaseTemplate(
                        UUID.randomUUID(),
                        phaseDto.getName(),
                        phaseDto.getDescription(),
                        phaseDto.getOrderIndex(),
                        phaseDto.getDefaultAssigneeRole());
                domain.addPhase(phase);
            });
        }

        // 3. Convertir a Entity (Dual Schema para las fases WBS empotradas en JSON)
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedBy(domain.getCreatedBy());

        try {
            String phasesJsonStr = objectMapper.writeValueAsString(domain.getPhases());
            entity.setPhasesJson(phasesJsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando las fases del WBS", e);
        }

        // 4. Persistir
        repository.save(entity);

        // 5. Devolver DTO actualizado con IDs
        dto.setId(domain.getId());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setCreatedBy(domain.getCreatedBy());

        if (dto.getPhases() != null) {
            for (int i = 0; i < dto.getPhases().size(); i++) {
                // Actualización menor por completitud
            }
        }

        return dto;
    }
}
