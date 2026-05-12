package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.ports.out.FormDesignPort;
import com.ibpms.poc.application.ports.out.ProcessEnginePort;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio Central del Motor de Formularios (Pantalla 7).
 * Orquesta CRUD, versionamiento inmutable y validación de borrados.
 * Totalmente alineado con Arquitectura Hexagonal (sin imports a infraestructura o librerías externas).
 */
@Service
@Transactional
public class FormDesignService {

    private final FormDesignPort formDesignPort;
    private final ProcessEnginePort processEnginePort;

    public FormDesignService(FormDesignPort formDesignPort, ProcessEnginePort processEnginePort) {
        this.formDesignPort = formDesignPort;
        this.processEnginePort = processEnginePort;
    }

    /**
     * Lista el catálogo general de formularios activos.
     */
    @Transactional(readOnly = true)
    public List<FormDesignDTO> listarCatalogo() {
        return formDesignPort.findAllActive();
    }

    /**
     * Obtiene el diseño histórico inmutable de una versión en específico.
     * Requerido para que instancias viejas en Camunda no rompan si el diseño cambia.
     */
    @Transactional(readOnly = true)
    @Traceability(US = "US-003", CA = {"CA-11", "CA-21"})
    public FormDesignDTO obtenerVersionInmutable(String technicalName, Integer version) {
        return formDesignPort.findByTechnicalNameAndVersion(technicalName, version)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));
    }

    /**
     * Listar todas las versiones pasadas y activas de un formulario base.
     */
    @Transactional(readOnly = true)
    @Traceability(US = "US-003", CA = {"CA-27"})
    public List<FormDesignDTO> listarVersiones(UUID formId) {
        FormDesignDTO base = formDesignPort.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));
        
        return formDesignPort.findAllByTechnicalName(base.getTechnicalName());
    }

    /**
     * Crea un formulario base (Definición Zero).
     */
    @Traceability(US = "US-003", CA = {"CA-01"})
    public FormDesignDTO crear(CreateFormDesignDTO dto, String userId) {
        Optional<FormDesignDTO> existing = formDesignPort.findTopByTechnicalNameOrderByVersionDesc(dto.getTechnicalName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Ya existe un formulario con el nombre técnico: " + dto.getTechnicalName());
        }

        FormDesignDTO newDto = new FormDesignDTO();
        newDto.setName(dto.getName());
        newDto.setTechnicalName(dto.getTechnicalName());
        newDto.setPattern(dto.getPattern());
        newDto.setVueTemplate(dto.getVueTemplate());
        newDto.setZodSchema(dto.getZodSchema());
        newDto.setFormFields(dto.getFormFields());
        newDto.setAuthorId(userId);

        return formDesignPort.createNew(newDto);
    }

    /**
     * Actualiza o bifurca la versión (CA-11).
     * Muta drafts o incrementa versión N+1 si el estado actual es ACTIVE.
     */
    @Traceability(US = "US-003", CA = {"CA-11"})
    public FormDesignDTO actualizarOCrearVersion(UUID id, CreateFormDesignDTO dto, String userId) {
        FormDesignDTO base = formDesignPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formulario base no encontrado"));

        if ("ACTIVE".equals(base.getStatus())) {
            // Regla CA-11: Imposible sobreescribir un formulario que el motor Camunda pueda estar usando.
            // Se genera una NUEVA entidad (Versión + 1)
            FormDesignDTO nuevaVersion = new FormDesignDTO();
            nuevaVersion.setName(dto.getName() != null ? dto.getName() : base.getName());
            nuevaVersion.setTechnicalName(base.getTechnicalName()); // El slug no cambia
            nuevaVersion.setPattern(base.getPattern()); // El patrón NO puede cambiar
            nuevaVersion.setVersion(base.getVersion() + 1);
            nuevaVersion.setVueTemplate(dto.getVueTemplate());
            nuevaVersion.setZodSchema(dto.getZodSchema());
            nuevaVersion.setFormFields(dto.getFormFields() != null ? dto.getFormFields() : base.getFormFields());
            nuevaVersion.setAuthorId(userId);

            return formDesignPort.saveVersion(nuevaVersion);
        } else {
            // Si es un borrador (DRAFT u otro), podemos mutarlo.
            base.setName(dto.getName() != null ? dto.getName() : base.getName());
            base.setVueTemplate(dto.getVueTemplate());
            base.setZodSchema(dto.getZodSchema());
            base.setFormFields(dto.getFormFields() != null ? dto.getFormFields() : base.getFormFields());
            base.setAuthorId(userId);
            
            return formDesignPort.saveVersion(base);
        }
    }

    /**
     * Soft-delete del formulario. Validado contra instancias activas en vuelo de Camunda.
     */
    @Traceability(US = "US-003", CA = {"CA-26"})
    public void eliminar(UUID id) {
        formDesignPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));

        // CA-26: Validación E2E contra Borrado Activo usando el puerto abstracto
        long activeProcessInstances = processEnginePort.countActiveProcessInstances();
        long activeTasksWithForm = processEnginePort.countActiveTasksWithForm();
                
        if (activeProcessInstances > 0 || activeTasksWithForm > 0) {
             throw new IllegalStateException("Formulario bloqueado (CA-26). El Motor Camunda reporta " + 
                     activeProcessInstances + " instancias de proceso activas y " + 
                     activeTasksWithForm + " tareas en vuelo que podrían usar este diseño.");
        }

        formDesignPort.updateStatusToDeleted(id);
    }
}
