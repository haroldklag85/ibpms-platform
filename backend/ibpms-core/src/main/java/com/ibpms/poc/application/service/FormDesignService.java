package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.port.out.FormDesignPort;
import com.ibpms.poc.application.port.out.ProcessEnginePort;
import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.domain.model.FormDefinition;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
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
    private final FormDefinitionPort formDefinitionPort;
    private final FormCertificationService formCertificationService;
    private final ObjectMapper objectMapper;

    public FormDesignService(FormDesignPort formDesignPort, 
                             ProcessEnginePort processEnginePort,
                             FormDefinitionPort formDefinitionPort,
                             FormCertificationService formCertificationService,
                             ObjectMapper objectMapper) {
        this.formDesignPort = formDesignPort;
        this.processEnginePort = processEnginePort;
        this.formDefinitionPort = formDefinitionPort;
        this.formCertificationService = formCertificationService;
        this.objectMapper = objectMapper;
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
     * Obtiene el formulario activo/más reciente por su nombre técnico (UAT B-04).
     */
    @Transactional(readOnly = true)
    public Optional<FormDesignDTO> obtenerPorTechnicalName(String technicalName) {
        return formDesignPort.findTopByTechnicalNameOrderByVersionDesc(technicalName);
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
     * Persiste la versión del diseño JSON completo en la tabla ibpms_form_definitions (CA-87).
     */
    @Traceability(US = "US-003", CA = {"CA-11", "CA-87"})
    public FormDesignDTO actualizarOCrearVersion(UUID id, CreateFormDesignDTO dto, String userId) {
        FormDesignDTO base = formDesignPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formulario base no encontrado"));

        // 1. Serializar formFields a JSON Schema
        String schemaContent = generateJsonSchema(dto.getFormFields());
        String hash = computeSha256(schemaContent);

        // 2. Detección de colisiones contra ibpms_form_definitions
        List<FormDefinition> existingDefs = formDefinitionPort.findByFormIdOrderByVersionIdDesc(id);
        boolean collision = existingDefs.stream().anyMatch(d -> hash.equals(d.getHashSha256()));
        if (collision) {
            throw new IllegalStateException("collision: A form version with the same visual schema hash already exists for this form.");
        }

        // 3. Determinar el siguiente versionId
        int nextVersionId = 1;
        if (!existingDefs.isEmpty()) {
            nextVersionId = existingDefs.get(0).getVersionId() + 1;
        }

        // 4. Persistir la nueva versión en ibpms_form_definitions sin sello QA (CA-13)
        formCertificationService.createNewVersion(id, nextVersionId, schemaContent, userId);

        // 5. Persistir en la tabla de diseño visual ibpms_form_design
        if ("ACTIVE".equals(base.getStatus())) {
            // Regla CA-11: Imposible sobreescribir un formulario que el motor Camunda pueda estar usando.
            // Se genera una NUEVA entidad (Versión + 1)
            FormDesignDTO nuevaVersion = new FormDesignDTO();
            nuevaVersion.setName(dto.getName() != null ? dto.getName() : base.getName());
            nuevaVersion.setTechnicalName(base.getTechnicalName()); // El slug no cambia
            nuevaVersion.setPattern(base.getPattern()); // El patrón NO puede cambiar
            nuevaVersion.setVersion(nextVersionId);
            nuevaVersion.setVueTemplate(dto.getVueTemplate());
            nuevaVersion.setZodSchema(dto.getZodSchema());
            nuevaVersion.setFormFields(dto.getFormFields() != null ? dto.getFormFields() : base.getFormFields());
            nuevaVersion.setAuthorId(userId);

            return formDesignPort.saveVersion(nuevaVersion);
        } else {
            // Si es un borrador (DRAFT u otro), podemos mutarlo.
            base.setName(dto.getName() != null ? dto.getName() : base.getName());
            base.setVersion(nextVersionId);
            base.setVueTemplate(dto.getVueTemplate());
            base.setZodSchema(dto.getZodSchema());
            base.setFormFields(dto.getFormFields() != null ? dto.getFormFields() : base.getFormFields());
            base.setAuthorId(userId);
            
            return formDesignPort.saveVersion(base);
        }
    }

    /**
     * Retorna el historial de versiones de definiciones persistidas en ibpms_form_definitions.
     */
    @Transactional(readOnly = true)
    @Traceability(US = "US-003", CA = {"CA-87"})
    public List<FormDefinition> listarVersionesDeDefinicion(UUID formId) {
        return formDefinitionPort.findByFormIdOrderByVersionIdDesc(formId);
    }

    private String generateJsonSchema(List<java.util.Map<String, Object>> fields) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode schemaNode = objectMapper.createObjectNode();
            schemaNode.put("$schema", "http://json-schema.org/draft-07/schema#");
            schemaNode.put("type", "object");

            com.fasterxml.jackson.databind.node.ObjectNode propertiesNode = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode requiredArray = objectMapper.createArrayNode();

            if (fields != null) {
                for (java.util.Map<String, Object> field : fields) {
                    String name = (String) field.get("camundaVariable");
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    com.fasterxml.jackson.databind.node.ObjectNode propNode = objectMapper.createObjectNode();
                    String type = (String) field.get("type");
                    if ("number".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
                        propNode.put("type", "integer");
                    } else if ("boolean".equalsIgnoreCase(type)) {
                        propNode.put("type", "boolean");
                    } else {
                        propNode.put("type", "string");
                    }
                    
                    propertiesNode.set(name, propNode);

                    // Validar si es obligatorio por su regla Zod
                    String zodRule = (String) field.get("zodRule");
                    if (zodRule != null && (zodRule.contains(".min(1)") || zodRule.contains(".nonempty"))) {
                        requiredArray.add(name);
                    }
                }
            }

            schemaNode.set("properties", propertiesNode);
            if (!requiredArray.isEmpty()) {
                schemaNode.set("required", requiredArray);
            }

            return objectMapper.writeValueAsString(schemaNode);
        } catch (Exception e) {
            return "{\"$schema\": \"http://json-schema.org/draft-07/schema#\",\"type\": \"object\"}";
        }
    }

    private String computeSha256(String content) {
        if (content == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Soft-delete del formulario. Validado contra instancias activas en vuelo de Camunda.
     */
    // @Traceability: US-005, CA-26
    @Traceability(US = "US-003", CA = {"CA-26"})
    public void eliminar(UUID id) {
        FormDesignDTO base = formDesignPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formulario no encontrado"));

        String technicalName = base.getTechnicalName();
        // CA-26: Validación E2E contra Borrado Activo usando el puerto abstracto
        long activeProcessInstances = processEnginePort.countActiveProcessInstances(technicalName);
        long activeTasksWithForm = processEnginePort.countActiveTasksWithForm(technicalName);
                
        if (activeProcessInstances > 0 || activeTasksWithForm > 0) {
             throw new IllegalStateException("Prohibido: Este formulario está siendo usado por " + activeProcessInstances + " procesos activos.");
        }

        formDesignPort.updateStatusToDeleted(id);
    }
}
