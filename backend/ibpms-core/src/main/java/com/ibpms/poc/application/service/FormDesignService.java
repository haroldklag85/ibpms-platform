package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio Central del Motor de Formularios (Pantalla 7).
 * Orquesta CRUD, versionamiento inmutable y validación de borrados.
 */
@Service
@Transactional
public class FormDesignService {

    private final FormDesignRepository formDesignRepository;

    public FormDesignService(FormDesignRepository formDesignRepository) {
        this.formDesignRepository = formDesignRepository;
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
        FormDesignEntity base = formDesignRepository.findById(id)
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
            nuevaVersion.setAuthorId(userId);

            formDesignRepository.save(nuevaVersion);
            return toDto(nuevaVersion);

        } else {
            // Si es un borrador (DRAFT), podemos mutarlo.
            base.setName(dto.getName() != null ? dto.getName() : base.getName());
            base.setVueTemplate(dto.getVueTemplate());
            base.setZodSchema(dto.getZodSchema());
            base.setUpdatedAt(LocalDateTime.now());
            base.setAuthorId(userId);
            formDesignRepository.save(base);
            return toDto(base);
        }
    }

    public void eliminar(UUID id) {
        FormDesignEntity entity = formDesignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));

        // CA-10: Eliminar form bloqueado si hay instancias activas.
        // Simulando llamada a Camunda:
        // long count =
        // camundaEngineTasks.countInstanciasConFormKey(entity.getTechnicalName());
        // if(count > 0) throw 403 HTTP.

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
        dto.setAuthorId(e.getAuthorId());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
