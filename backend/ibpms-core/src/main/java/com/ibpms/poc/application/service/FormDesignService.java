package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormFieldMetadataDTO;

/**
 * Servicio Central del Motor de Formularios (Pantalla 7).
 * Orquesta CRUD, versionamiento inmutable y validación de borrados.
 */
@Service
@Transactional
public class FormDesignService {

    private final FormDesignRepository formDesignRepository;
    private final ObjectMapper objectMapper;
    private final org.camunda.bpm.engine.RuntimeService runtimeService;
    private final org.camunda.bpm.engine.HistoryService historyService;

    public FormDesignService(FormDesignRepository formDesignRepository, 
                             ObjectMapper objectMapper,
                             org.camunda.bpm.engine.RuntimeService runtimeService,
                             org.camunda.bpm.engine.HistoryService historyService) {
        this.formDesignRepository = formDesignRepository;
        this.objectMapper = objectMapper;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    /**
     * Lista todos los formularios (solo su última versión válida).
     * En una implementación real, aquí se agruparía por technicalName
     * devolviendo el top 1 de la versión.
     */
    @Transactional(readOnly = true)
    public List<FormDesignDTO> listarCatalogo() {
        // Para V1 simple: listar todos los activos
        return formDesignRepository.findAll().stream()
                .filter(f -> f.getStatus() != FormDesignEntity.Status.DELETED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el diseño histórico inmutable de una versión en específico.
     * Requerido (CA-11, CA-21) para que instancias viejas en Camunda
     * no rompan si el diseño cambia en V2.
     */
    @Transactional(readOnly = true)
    public FormDesignDTO obtenerVersionInmutable(String technicalName, Integer version) {
        return formDesignRepository.findByTechnicalNameAndVersion(technicalName, version)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));
    }

    /**
     * CA-27: Listar todas las versiones pasadas y activas de un formulario.
     */
    @Transactional(readOnly = true)
    public List<FormDesignDTO> listarVersiones(UUID formId) {
        FormDesignEntity entity = formDesignRepository.findById(java.util.Objects.requireNonNull(formId))
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));
        
        return formDesignRepository.findAll().stream()
                .filter(f -> f.getTechnicalName().equals(entity.getTechnicalName()))
                .sorted(java.util.Comparator.comparing(FormDesignEntity::getVersion).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Crea un formulario desde cero (Definición Base de Patrón).
     */
    public FormDesignDTO crear(CreateFormDesignDTO dto, String userId) {
        Optional<FormDesignEntity> existing = formDesignRepository
                .findTopByTechnicalNameOrderByVersionDesc(dto.getTechnicalName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un formulario con el nombre técnico: " + dto.getTechnicalName());
        }

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
        entity.setAuthorId(userId);

        formDesignRepository.save(entity);
        return toDto(entity);
    }

    /**
     * Actualizar / Guardar Nueva Versión (CA-11).
     * Nunca hace UPDATE sobre una versión que ya esté "ACTIVE". Al guardar
     * un formulario activo, clona en una versión N+1 como DRAFT.
     */
    public FormDesignDTO actualizarOCrearVersion(UUID id, CreateFormDesignDTO dto, String userId) {
        FormDesignEntity base = formDesignRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new EntityNotFoundException("Formulario base no encontrado"));

        if (base.getStatus() == FormDesignEntity.Status.ACTIVE) {
            // Regla CA-11: Imposible sobreescribir un formulario que el motor Camunda pueda
            // estar usando.
            // Se genera una NUEVA entidad (Versión + 1)
            FormDesignEntity nuevaVersion = new FormDesignEntity();
            nuevaVersion.setName(dto.getName() != null ? dto.getName() : base.getName());
            nuevaVersion.setTechnicalName(base.getTechnicalName()); // El slug no cambia
            nuevaVersion.setPattern(base.getPattern()); // El patrón NO puede cambiar
            nuevaVersion.setVersion(base.getVersion() + 1);
            nuevaVersion.setVueTemplate(dto.getVueTemplate());
            nuevaVersion.setZodSchema(dto.getZodSchema());
            try {
                nuevaVersion.setFormFields(dto.getFormFields() != null ? objectMapper.writeValueAsString(dto.getFormFields()) : base.getFormFields());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando formFields", e);
            }
            nuevaVersion.setAuthorId(userId);

            formDesignRepository.save(nuevaVersion);
            return toDto(nuevaVersion);

        } else {
            // Si es un borrador (DRAFT), podemos mutarlo.
            base.setName(dto.getName() != null ? dto.getName() : base.getName());
            base.setVueTemplate(dto.getVueTemplate());
            base.setZodSchema(dto.getZodSchema());
            try {
                base.setFormFields(dto.getFormFields() != null ? objectMapper.writeValueAsString(dto.getFormFields()) : base.getFormFields());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando formFields", e);
            }
            base.setUpdatedAt(LocalDateTime.now());
            base.setAuthorId(userId);
            formDesignRepository.save(base);
            return toDto(base);
        }
    }

    public void eliminar(UUID id) {
        FormDesignEntity entity = formDesignRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));

        // CA-26: Validación E2E contra Borrado Activo
        long activeProcessInstances = runtimeService.createProcessInstanceQuery().active().count();
        long activeTasksWithForm = historyService.createHistoricTaskInstanceQuery()
                .unfinished()
                .count(); // En un caso real buscaríamos por formKey especifico. Para efectos de V1, validamos si el Engine reporta actividad viva.
                
        if (activeProcessInstances > 0 || activeTasksWithForm > 0) {
             throw new IllegalStateException("Formulario bloqueado (CA-26). El Motor Camunda reporta " + 
                     activeProcessInstances + " instancias de proceso activas y " + 
                     activeTasksWithForm + " tareas en vuelo que podrían usar este diseño.");
        }

        entity.setStatus(FormDesignEntity.Status.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        formDesignRepository.save(entity);
    }

    private FormDesignDTO toDto(FormDesignEntity e) {
        FormDesignDTO dto = new FormDesignDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setTechnicalName(e.getTechnicalName());
        dto.setPattern(e.getPattern().name());
        dto.setStatus(e.getStatus().name());
        dto.setVersion(e.getVersion());
        dto.setVueTemplate(e.getVueTemplate());
        dto.setZodSchema(e.getZodSchema());
        if (e.getFormFields() != null) {
            try {
                dto.setFormFields(objectMapper.readValue(e.getFormFields(), new TypeReference<List<FormFieldMetadataDTO>>() {}));
            } catch (JsonProcessingException ex) {
                dto.setFormFields(Collections.emptyList());
            }
        }
        dto.setAuthorId(e.getAuthorId());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
