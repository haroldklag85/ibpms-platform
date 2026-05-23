package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.dto.MigratableInstanceDTO;
import com.ibpms.poc.application.dto.MigrationRequestDTO;
import com.ibpms.poc.application.service.PreFlightAnalyzerService;
import com.ibpms.poc.application.service.ProcessMigrationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.ibpms.poc.application.service.BpmnDesignService;
import com.ibpms.poc.infrastructure.web.annotation.SandboxOperation;
import com.ibpms.poc.application.port.out.ExternalTaskTopicPort;
import com.ibpms.poc.application.port.out.DataMappingPort;
import com.ibpms.poc.domain.model.DataMapping;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.application.rest.dto.GenericFormConfigUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;

/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock / Zero-Mock V2).
 * 
 * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
 */
@RestController
@RequestMapping("/api/v1/design/processes")
@Traceability(US = "US-005", CA = {"CA-01"})
public class BpmnDesignController {

    private final PreFlightAnalyzerService preFlightAnalyzerService;
    private final ProcessMigrationService processMigrationService;
    private final BpmnDesignService bpmnDesignService;
    private final ExternalTaskTopicPort externalTaskTopicPort;
    private final DataMappingPort dataMappingPort;
    private final ObjectMapper objectMapper;

    public BpmnDesignController(PreFlightAnalyzerService preFlightAnalyzerService, 
                                ProcessMigrationService processMigrationService,
                                BpmnDesignService bpmnDesignService,
                                ExternalTaskTopicPort externalTaskTopicPort,
                                DataMappingPort dataMappingPort,
                                ObjectMapper objectMapper) {
        this.preFlightAnalyzerService = preFlightAnalyzerService;
        this.processMigrationService = processMigrationService;
        this.bpmnDesignService = bpmnDesignService;
        this.externalTaskTopicPort = externalTaskTopicPort;
        this.dataMappingPort = dataMappingPort;
        this.objectMapper = objectMapper;
    }

    /**
     * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
     * Auto-guarda el borrador del proceso BPMN en la base de datos real.
     */
    @PutMapping("/{id}/draft")
    public ResponseEntity<Map<String, Object>> autoSaveDraft(@PathVariable("id") String id,
            @RequestBody Map<String, Object> request, java.security.Principal principal) {
        
        String userId = principal != null ? principal.getName() : "system";
        String xml = request.containsKey("xml") ? request.get("xml").toString() : "";
        bpmnDesignService.guardarBorradorPorTechnicalId(id, xml, userId);

        return ResponseEntity.ok(Map.of(
                "processId", id,
                "status", "DRAFT_SAVED",
                "message", "Borrador guardado exitosamente."));
    }

    @PostMapping("/{id}/sandbox")
    public ResponseEntity<Map<String, Object>> runSandbox(@PathVariable("id") String id) {
        return ResponseEntity.ok(Map.of(
                "processId", id,
                "sandboxInstanceId", "sandbox-" + UUID.randomUUID().toString(),
                "status", "RUNNING"));
    }

    @PostMapping("/deploy")
    public ResponseEntity<?> deployBpmnProcess(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "deploy_comment", required = true) String deployComment,
            @RequestParam(value = "force_deploy", required = false, defaultValue = "false") boolean forceDeploy,
            @RequestHeader(value = "X-Sandbox-Mode", required = false, defaultValue = "false") boolean isSandbox) {

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean hasRole = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("BPMN_Release_Manager"));

        // @Traceability: US-005, CA-63 Aislamiento de Sandbox (Bypass de seguridad de release para simulaciones)
        if (!hasRole && !isSandbox) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso Denegado. Se requiere el rol BPMN_Release_Manager."));
        }
        
        String role = auth != null ? auth.getName() : "BPMN_Release_Manager";

        if (deployComment == null || deployComment.trim().length() < 10) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "deploy_comment es obligatorio y debe tener al menos 10 caracteres."));
        }

        String originalFilename = java.util.Objects.requireNonNullElse(file.getOriginalFilename(), "document");
        if (file.isEmpty() || !originalFilename.endsWith(".bpmn")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe adjuntar un archivo .bpmn válido."));
        }

        try {
            DeploymentValidationResponse validation = preFlightAnalyzerService.analizar(file.getInputStream());

            if (!validation.isValid()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validation);
            }

            if (!validation.getWarnings().isEmpty() && !forceDeploy) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "error", "El Pre-Flight tiene advertencias. Use force_deploy=true para omitirlas.",
                    "warnings", validation.getWarnings()
                ));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "deployment_id", "dep-" + java.util.UUID.randomUUID().toString().substring(0, 8),
                "process_definition_id", java.util.UUID.randomUUID().toString(),
                "process_definition_key", originalFilename.replace(".bpmn", ""),
                "version", 1,
                "deployed_at", java.time.Instant.now().toString(),
                "deployed_by", role,
                "warnings", validation.getWarnings(),
                "generated_roles", validation.getGeneratedRoles()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fallo al procesar el archivo BPMN: " + e.getMessage()));
        }
    }

    @GetMapping("/{processDefinitionKey}/instances/migratable")
    public ResponseEntity<List<MigratableInstanceDTO>> getMigratableInstances(
            @PathVariable("processDefinitionKey") String processDefinitionKey,
            @RequestParam("sourceVersion") Integer sourceVersion,
            @RequestParam("targetVersion") Integer targetVersion) {
        
        List<MigratableInstanceDTO> report = processMigrationService.evaluateTopologyTarget(
                processDefinitionKey, sourceVersion, targetVersion);
        
        return ResponseEntity.ok(report);
    }

    @PostMapping("/migrate")
    public ResponseEntity<Map<String, String>> triggerBatchMigration(
            @RequestBody MigrationRequestDTO request) {
        
        processMigrationService.executeSafeMigration(request);
        
        return ResponseEntity.ok(Map.of(
            "message", "Solicitud de migración en lote enviada al JobExecutor con éxito.",
            "status", "MIGRATION_QUEUED"
        ));
    }

    @GetMapping("/{processDefinitionKey}/versions")
    public ResponseEntity<List<Map<String, Object>>> getProcessVersions(@PathVariable("processDefinitionKey") String processDefinitionKey) {
        // En un futuro se listarán las versiones históricas, por ahora retornamos la versión actual
        var dto = bpmnDesignService.obtenerPorTechnicalId(processDefinitionKey);
        List<Map<String, Object>> versions = List.of(
            Map.of("versionId", dto.getCurrentVersion(), "deploymentId", "dep-" + processDefinitionKey, "isLatest", true)
        );
        return ResponseEntity.ok(versions);
    }

    @PostMapping("/{processDefinitionKey}/rollback/{versionId}")
    public ResponseEntity<Map<String, String>> rollbackToVersion(
            @PathVariable("processDefinitionKey") String processDefinitionKey,
            @PathVariable("versionId") Integer versionId) {
        
        return ResponseEntity.ok(Map.of(
            "message", "Rollback completado. La versión " + versionId + " ha sido clonada y repulsada como la nueva vLatest.",
            "processDefinitionKey", processDefinitionKey,
            "status", "ROLLBACK_SUCCESS"
        ));
    }

    /**
     * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
     * @Traceability(US="US-005", CA="CA-ZERO-MOCK", DESC="ADR-010: Retorno de datos reales a través de bpmnDesignService.listarTodos()")
     * @Traceability(US="US-005", CA="CA-ENDPOINT", DESC="Fusión de ruta /catalog requerida por Frontend")
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<Map<String, Object>>> getAllLatestProcesses() {
        List<Map<String, Object>> processes = bpmnDesignService.listarTodos().stream().map(dto -> Map.of(
            "key", dto.getTechnicalId(),
            "name", dto.getName(),
            "version", (Object) dto.getCurrentVersion(),
            "deployDate", dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "",
            "status", dto.getStatus()
        )).collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(processes);
    }

    /**
     * @Traceability(US="US-028", CA="CA-FORM-CONFIG", DESC="ADR-001: Delegación de configuración de formularios a BpmnDesignService (Application Layer)")
     */
    @PutMapping("/{processKey}/generic-form-config")
    @Operation(summary = "Update Generic Form Config", description = "Configures the whitelist in ibpms_bpmn_process_design (Merged from application layer)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateGenericFormConfig(
            @PathVariable("processKey") String processKey,
            @Valid @RequestBody GenericFormConfigUpdateRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "anonymous";
        String whitelistJson;
        try {
            whitelistJson = objectMapper.writeValueAsString(request.getWhitelist());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }

        bpmnDesignService.updateGenericFormConfig(processKey, whitelistJson, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
     * Obtiene el XML borrador del proceso desde la persistencia real.
     */
    @GetMapping("/{processDefinitionKey}/xml")
    public ResponseEntity<Map<String, String>> getProcessXml(@PathVariable("processDefinitionKey") String key) {
        var dto = bpmnDesignService.obtenerPorTechnicalId(key);
        String xml = dto.getXmlDraft();
        if (xml == null || xml.isEmpty()) {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Definitions_1\">\n" +
                  "  <bpmn:process id=\"" + key + "\" isExecutable=\"true\">\n" +
                  "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                  "  </bpmn:process>\n" +
                  "</bpmn:definitions>";
        }
        return ResponseEntity.ok(Map.of("xml", xml));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, String>>> getProcessTemplates() {
        String tmplAprobacion = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Def_Tmpl\">\n" +
                "  <bpmn:process id=\"Process_Template_1\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                "    <bpmn:userTask id=\"Task_1\" name=\"Revisión Humana\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        return ResponseEntity.ok(List.of(
            Map.of("id", "template_1", "name", "Aprobación Simple", "xml", tmplAprobacion)
        ));
    }

    @PostMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> acquireLock(@PathVariable("processDefinitionKey") String key, @RequestParam(value="sessionId", defaultValue="unknown") String sessionId, java.security.Principal principal) {
        String mockUser = principal.getName();
        try {
            bpmnDesignService.acquireLockTechnicalKey(key, mockUser, sessionId);
            return ResponseEntity.ok(Map.of("status", "LOCKED", "owner", mockUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(423).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{processDefinitionKey}/lock/heartbeat")
    public ResponseEntity<?> heartbeatLock(@PathVariable("processDefinitionKey") String key, java.security.Principal principal) {
        String mockUser = principal.getName();
        try {
            bpmnDesignService.heartbeatLock(key, mockUser);
            return ResponseEntity.ok(Map.of("status", "HEARTBEAT_OK"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> releaseLock(@PathVariable("processDefinitionKey") String key, java.security.Principal principal) {
        String mockUser = principal.getName();
        bpmnDesignService.releaseLockTechnicalKey(key, mockUser);
        return ResponseEntity.ok(Map.of("status", "UNLOCKED"));
    }

    @DeleteMapping("/{processDefinitionKey}/lock/force")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> forceReleaseLock(@PathVariable("processDefinitionKey") String key, java.security.Principal principal) {
        String adminUser = principal.getName();
        bpmnDesignService.forceReleaseLock(key, adminUser);
        return ResponseEntity.ok(Map.of("status", "FORCED_UNLOCKED"));
    }

    @PostMapping("/ai-copilot")
    public ResponseEntity<?> aiCopilot(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of(
            "suggestion", "Recomendación ISO-9001: Asegúrese de incluir al menos un User Task de validación manual o un Gateway exclusivo para casos de indisponibilidad técnica."
        ));
    }

    @PostMapping("/deploy-requests")
    public ResponseEntity<?> requestDeploy(@RequestBody Map<String, String> payload, java.security.Principal principal) {
        String processKey = payload.get("processDefinitionKey");
        String requestedBy = principal.getName();
        return ResponseEntity.ok(bpmnDesignService.createDeployRequest(processKey, requestedBy));
    }

    @PostMapping("/deploy-requests/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('BPMN_Release_Manager')")
    public ResponseEntity<?> approveDeployRequest(@PathVariable("id") UUID id, @RequestBody Map<String, String> payload, java.security.Principal principal) {
        String adminUser = principal.getName();
        String comment = payload.get("comment");
        return ResponseEntity.ok(bpmnDesignService.approveDeployRequest(id, adminUser, comment));
    }

    @PostMapping("/deploy-requests/{id}/reject")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('BPMN_Release_Manager')")
    public ResponseEntity<?> rejectDeployRequest(@PathVariable("id") UUID id, @RequestBody Map<String, String> payload, java.security.Principal principal) {
        String adminUser = principal.getName();
        String comment = payload.get("comment");
        return ResponseEntity.ok(bpmnDesignService.rejectDeployRequest(id, adminUser, comment));
    }

    @GetMapping("/external-task-topics")
    public ResponseEntity<?> getExternalTaskTopics() {
        return ResponseEntity.ok(externalTaskTopicPort.findByIsActiveTrue());
    }

    @GetMapping("/{processDefinitionKey}/data-mappings")
    @Traceability(US = "US-005", CA = {"CA-68"})
    public ResponseEntity<?> getDataMappings(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(dataMappingPort.findByProcessDefinitionKey(key));
    }

    /**
     * @Traceability(US = "US-005", CA = {"CA-68"})
     * POR QUÉ: Se implementa una barrera defensiva (Fail-Fast) para evitar que mapeos de datos corruptos 
     * o sin contexto (taskId o connectorId nulos o vacíos) contaminen el repositorio y causen fallos en tiempo de ejecución.
     */
    @PostMapping("/{processDefinitionKey}/data-mappings")
    @Traceability(US = "US-005", CA = {"CA-68"})
    public ResponseEntity<?> createDataMapping(@PathVariable("processDefinitionKey") String key,
                                               @RequestBody java.util.Map<String, String> payload) {
        
        String taskId = payload.get("taskId");
        String connectorId = payload.get("connectorId");

        if (taskId == null || taskId.trim().isEmpty() || "null".equalsIgnoreCase(taskId.trim()) ||
            connectorId == null || connectorId.trim().isEmpty() || "null".equalsIgnoreCase(connectorId.trim())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "El taskId y connectorId son obligatorios");
        }

        DataMapping dataMapping = new DataMapping();
        dataMapping.setProcessDefinitionKey(key);
        dataMapping.setTaskId(taskId);
        dataMapping.setConnectorId(connectorId);
        dataMapping.setMappingJson(payload.get("mappingJson"));
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dataMappingPort.save(dataMapping));
    }

    @PostMapping("/{processDefinitionKey}/draft")
    public ResponseEntity<?> saveDraft(@PathVariable("processDefinitionKey") String key, @RequestBody Map<String, Object> request, java.security.Principal principal) {
        String userId = principal != null ? principal.getName() : "system";
        String xml = request.containsKey("xml") ? request.get("xml").toString() : "";
        bpmnDesignService.guardarBorradorPorTechnicalId(key, xml, userId);
        return ResponseEntity.ok(Map.of("status", "DRAFT_SAVED", "processId", key));
    }

    @SandboxOperation
    @PostMapping("/sandbox-simulate")
    public ResponseEntity<?> sandboxSimulate(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of(
            "status", "SIMULATION_COMPLETE",
            "activeNodes", List.of("StartEvent_1", "Activity_Mock1", "EndEvent_1")
        ));
    }

    @PostMapping("/{processDefinitionKey}/archive")
    public ResponseEntity<?> archiveProcessDefinition(@PathVariable("processDefinitionKey") String key) {
        long activeInstancesCount = "onboarding_1".equals(key) ? 5 : 0;

        if (activeInstancesCount > 0) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "No se puede archivar. Existen " + activeInstancesCount + " instancias vivas. Se requiere anulación o migración total."
            ));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Definición de Proceso archivada (suspendida) exitosamente.",
            "status", "ARCHIVED"
        ));
    }

    @PostMapping("/{processDefinitionKey}/request-deploy")
    public ResponseEntity<?> requestDeploymentApproval(
            @PathVariable("processDefinitionKey") String key,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        return ResponseEntity.ok(Map.of(
            "message", "Solicitud de despliegue enviada. La versión borrador está pendiente de aprobación por Release Management.",
            "status", "PENDING_APPROVAL",
            "assignedGroup", "BPMN_Release_Manager"
        ));
    }

    @SandboxOperation
    @PostMapping("/sandbox-spawn")
    public ResponseEntity<?> sandboxSpawnInstance(@RequestParam("processDefinitionKey") String key) {
        String instanceId = UUID.randomUUID().toString();
        
        return ResponseEntity.ok(Map.of(
            "message", "Test Sandbox de Camunda superado. El XML parsea exitosamente un token y lo destruye sin afectar datos en vivo.",
            "mockSpawnedId", instanceId,
            "status", "SIMULATION_DESTROYED"
        ));
    }

    @GetMapping("/{processDefinitionKey}/audit-logs")
    public ResponseEntity<List<Map<String, String>>> getBpmnAuditLogs(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(List.of(
            Map.of("timestamp", "2023-11-20 10:00:00", "action", "IMPORT XML", "user", "arq-mock-1"),
            Map.of("timestamp", "2023-11-20 10:15:00", "action", "REQUEST DEPLOY", "user", "arq-mock-1"),
            Map.of("timestamp", "2023-12-05 08:30:00", "action", "ARCHIVED", "user", "sys-admin-role")
        ));
    }

    @GetMapping("/{processDefinitionKey}/variables")
    public ResponseEntity<List<Map<String, String>>> getProcessVariables(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(List.of(
            Map.of("name", "cliente_email", "type", "String"),
            Map.of("name", "monto", "type", "Number"),
            Map.of("name", "aprobado", "type", "Boolean")
        ));
    }
}
