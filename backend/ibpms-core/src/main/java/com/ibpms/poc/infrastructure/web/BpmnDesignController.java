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
import com.ibpms.poc.infrastructure.jpa.repository.ExternalTaskTopicRepository;

/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/design/processes")
public class BpmnDesignController {

    private final PreFlightAnalyzerService preFlightAnalyzerService;
    private final ProcessMigrationService processMigrationService;
    private final BpmnDesignService bpmnDesignService;
    private final ExternalTaskTopicRepository externalTaskTopicRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.DataMappingRepository dataMappingRepository;

    public BpmnDesignController(PreFlightAnalyzerService preFlightAnalyzerService, 
                                ProcessMigrationService processMigrationService,
                                BpmnDesignService bpmnDesignService,
                                ExternalTaskTopicRepository externalTaskTopicRepository,
                                com.ibpms.poc.infrastructure.jpa.repository.DataMappingRepository dataMappingRepository) {
        this.preFlightAnalyzerService = preFlightAnalyzerService;
        this.processMigrationService = processMigrationService;
        this.bpmnDesignService = bpmnDesignService;
        this.externalTaskTopicRepository = externalTaskTopicRepository;
        this.dataMappingRepository = dataMappingRepository;
    }

    @PutMapping("/{id}/draft")
    public ResponseEntity<Map<String, Object>> autoSaveDraft(@PathVariable("id") String id,
            @RequestBody Map<String, Object> request) {
        // Mock Implementation for Auto-Save
        return ResponseEntity.ok(Map.of(
                "processId", id,
                "status", "DRAFT_SAVED",
                "message", "Borrador guardado exitosamente."));
    }

    @PostMapping("/{id}/sandbox")
    public ResponseEntity<Map<String, Object>> runSandbox(@PathVariable("id") String id) {
        // Mock Implementation for Sandbox testing
        return ResponseEntity.ok(Map.of(
                "processId", id,
                "sandboxInstanceId", "sandbox-" + UUID.randomUUID().toString(),
                "status", "RUNNING"));
    }

    @PostMapping("/deploy")
    public ResponseEntity<?> deployBpmnProcess(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Mock-Role", required = false, defaultValue = "GUEST") String role) {
        
        // CA-21: Escudo RBAC para el Despliegue
        if (!"BPMN_Release_Manager".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso Denegado. Se requiere el rol BPMN_Release_Manager para comisionar modelos en Producción"));
        }

        String originalFilename = java.util.Objects.requireNonNullElse(file.getOriginalFilename(), "document");
        if (file.isEmpty() || !originalFilename.endsWith(".bpmn")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe adjuntar un archivo .bpmn válido."));
        }

        try {
            DeploymentValidationResponse validation = preFlightAnalyzerService.analizar(file.getInputStream());
            
            if (!validation.isValid()) {
                // CA-2: Arrojar HTTP 422 si hay errores de validación
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validation);
            }

            // Aquí se ejecutaría el despliegue al motor Camunda
            // Por V1 (Mock): simulate deploy
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Proceso desplegado exitosamente.",
                "warnings", validation.getWarnings(),
                "generatedRoles", validation.getGeneratedRoles()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fallo al procesar el archivo BPMN: " + e.getMessage()));
        }
    }

    /**
     * CA-9: Evaluador Topológico de Instancias de versión vieja hacia versión nueva.
     */
    @GetMapping("/{processDefinitionKey}/instances/migratable")
    public ResponseEntity<List<MigratableInstanceDTO>> getMigratableInstances(
            @PathVariable("processDefinitionKey") String processDefinitionKey,
            @RequestParam("sourceVersion") Integer sourceVersion,
            @RequestParam("targetVersion") Integer targetVersion) {
        
        List<MigratableInstanceDTO> report = processMigrationService.evaluateTopologyTarget(
                processDefinitionKey, sourceVersion, targetVersion);
        
        return ResponseEntity.ok(report);
    }

    /**
     * CA-7 y CA-10: Ejecutor Transaccional de Migración Grandfathering
     * @param request El payload blindado MigrationRequestDTO garantiza que "variables" es ignorado localmente.
     */
    @PostMapping("/migrate")
    public ResponseEntity<Map<String, String>> triggerBatchMigration(
            @RequestBody MigrationRequestDTO request) {
        
        processMigrationService.executeSafeMigration(request);
        
        return ResponseEntity.ok(Map.of(
            "message", "Solicitud de migración en lote enviada al JobExecutor con éxito.",
            "status", "MIGRATION_QUEUED"
        ));
    }

    /**
     * CA-15: Listado Cronológico de Versiones Desplegadas.
     */
    @GetMapping("/{processDefinitionKey}/versions")
    public ResponseEntity<List<Map<String, Object>>> getProcessVersions(@PathVariable("processDefinitionKey") String processDefinitionKey) {
        // MOCK DB Lookup
        List<Map<String, Object>> versions = List.of(
            Map.of("versionId", 2, "deploymentId", "dep-888", "isLatest", true),
            Map.of("versionId", 1, "deploymentId", "dep-777", "isLatest", false)
        );
        return ResponseEntity.ok(versions);
    }

    /**
     * CA-15: Rollback Un-Clic. Redeploy idéntico a una versión previa rescatada.
     */
    @PostMapping("/{processDefinitionKey}/rollback/{versionId}")
    public ResponseEntity<Map<String, String>> rollbackToVersion(
            @PathVariable("processDefinitionKey") String processDefinitionKey,
            @PathVariable("versionId") Integer versionId) {
        
        // El motor extraeria el XML de la version N y haria un `repositoryService.createDeployment()`
        return ResponseEntity.ok(Map.of(
            "message", "Rollback completado. La versión " + versionId + " ha sido clonada y repulsada como la nueva vLatest.",
            "processDefinitionKey", processDefinitionKey,
            "status", "ROLLBACK_SUCCESS"
        ));
    }

    /**
     * CA-23: Catálogo de Modelos Base (Mock RepositoryService)
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLatestProcesses() {
        return ResponseEntity.ok(List.of(
            Map.of("key", "onboarding_1", "name", "Alta de Colaboradores", "version", 2, "deployDate", "2023-11-20T10:00:00Z"),
            Map.of("key", "invoice_proc", "name", "Procesamiento de Facturas", "version", 5, "deployDate", "2023-12-01T15:30:00Z")
        ));
    }

    /**
     * CA-23 Click: Extracción RAW XML para pintado en Lienzo
     */
    @GetMapping("/{processDefinitionKey}/xml")
    public ResponseEntity<Map<String, String>> getProcessXml(@PathVariable("processDefinitionKey") String key) {
        String mockXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Definitions_1\">\n" +
                         "  <bpmn:process id=\"" + key + "\" isExecutable=\"true\">\n" +
                         "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                         "  </bpmn:process>\n" +
                         "</bpmn:definitions>";
        return ResponseEntity.ok(Map.of("xml", mockXml));
    }

    /**
     * CA-27: Proveedor de Plantillas BPMN
     */
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
    /**
     * CA-66: Bloqueo Pesimista (Adquisición) con DB y Heartbeat
     */
    @PostMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> acquireLock(@PathVariable("processDefinitionKey") String key, @RequestParam(value="sessionId", defaultValue="unknown") String sessionId) {
        String mockUser = "user-mock-123";
        try {
            bpmnDesignService.acquireLockTechnicalKey(key, mockUser, sessionId);
            return ResponseEntity.ok(Map.of("status", "LOCKED", "owner", mockUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(423).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * CA-66: Bloqueo Pesimista (Heartbeat)
     */
    @PostMapping("/{processDefinitionKey}/lock/heartbeat")
    public ResponseEntity<?> heartbeatLock(@PathVariable("processDefinitionKey") String key) {
        String mockUser = "user-mock-123";
        try {
            bpmnDesignService.heartbeatLock(key, mockUser);
            return ResponseEntity.ok(Map.of("status", "HEARTBEAT_OK"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * CA-66: Bloqueo Pesimista (Liberación)
     */
    @DeleteMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> releaseLock(@PathVariable("processDefinitionKey") String key) {
        String mockUser = "user-mock-123";
        bpmnDesignService.releaseLockTechnicalKey(key, mockUser);
        return ResponseEntity.ok(Map.of("status", "UNLOCKED"));
    }

    /**
     * CA-64: Break-lock de Emergencia
     */
    @DeleteMapping("/{processDefinitionKey}/lock/force")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> forceReleaseLock(@PathVariable("processDefinitionKey") String key) {
        String adminUser = "admin-mock-123"; // TODO: Obtain from token context
        bpmnDesignService.forceReleaseLock(key, adminUser);
        return ResponseEntity.ok(Map.of("status", "FORCED_UNLOCKED"));
    }

    /**
     * CA-17: Copiloto IA (Mock)
     */
    @PostMapping("/ai-copilot")
    public ResponseEntity<?> aiCopilot(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of(
            "suggestion", "Recomendación ISO-9001: Asegúrese de incluir al menos un User Task de validación manual o un Gateway exclusivo para casos de indisponibilidad técnica."
        ));
    }

    /**
     * CA-69: Request Deploy
     */
    @PostMapping("/deploy-requests")
    public ResponseEntity<?> requestDeploy(@RequestBody Map<String, String> payload) {
        String processKey = payload.get("processDefinitionKey");
        String requestedBy = "user-mock-123"; // TODO: Token
        return ResponseEntity.ok(bpmnDesignService.createDeployRequest(processKey, requestedBy));
    }

    /**
     * CA-69: Approve Deploy Request
     */
    @PostMapping("/deploy-requests/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> approveDeployRequest(@PathVariable("id") UUID id, @RequestBody Map<String, String> payload) {
        String adminUser = "admin-mock-123";
        String comment = payload.get("comment");
        return ResponseEntity.ok(bpmnDesignService.approveDeployRequest(id, adminUser, comment));
    }

    /**
     * CA-69: Reject Deploy Request
     */
    @PostMapping("/deploy-requests/{id}/reject")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> rejectDeployRequest(@PathVariable("id") UUID id, @RequestBody Map<String, String> payload) {
        String adminUser = "admin-mock-123";
        String comment = payload.get("comment");
        return ResponseEntity.ok(bpmnDesignService.rejectDeployRequest(id, adminUser, comment));
    }

    /**
     * CA-70: Catálogo de External Task Topics
     */
    @GetMapping("/external-task-topics")
    public ResponseEntity<?> getExternalTaskTopics() {
        return ResponseEntity.ok(externalTaskTopicRepository.findByIsActiveTrue());
    }

    /**
     * CA-68: Data Mappings
     */
    @GetMapping("/{processDefinitionKey}/data-mappings")
    public ResponseEntity<?> getDataMappings(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(dataMappingRepository.findByProcessDefinitionKey(key));
    }

    @PostMapping("/{processDefinitionKey}/data-mappings")
    public ResponseEntity<?> createDataMapping(@PathVariable("processDefinitionKey") String key,
                                               @RequestBody com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity entity) {
        entity.setProcessDefinitionKey(key);
        return ResponseEntity.ok(dataMappingRepository.save(entity));
    }

    /**
     * CA-19: Autosave Borrador (POST explícito)
     */
    @PostMapping("/{processDefinitionKey}/draft")
    public ResponseEntity<?> saveDraft(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(Map.of("status", "DRAFT_SAVED", "processId", key));
    }

    /**
     * CA-20: Sandbox Simulator (Extrae estáticamente 3 nodos a animar)
     */
    @SandboxOperation
    @PostMapping("/sandbox-simulate")
    public ResponseEntity<?> sandboxSimulate(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of(
            "status", "SIMULATION_COMPLETE",
            "activeNodes", List.of("StartEvent_1", "Activity_Mock1", "EndEvent_1")
        ));
    }

    /**
     * CA-32: Suspender/Archivar Diagramas Seguros (Anti-Deadlock)
     */
    @PostMapping("/{processDefinitionKey}/archive")
    public ResponseEntity<?> archiveProcessDefinition(@PathVariable("processDefinitionKey") String key) {
        // MOCK Camunda API: long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(key).count();
        long activeInstancesCount = "onboarding_1".equals(key) ? 5 : 0; // Simulador: onboarding tiene instancias, otros no.

        if (activeInstancesCount > 0) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "No se puede archivar. Existen " + activeInstancesCount + " instancias vivas. Se requiere anulación o migración total."
            ));
        }

        // MOCK Camunda API: repositoryService.suspendProcessDefinitionByKey(key);
        return ResponseEntity.ok(Map.of(
            "message", "Definición de Proceso archivada (suspendida) exitosamente.",
            "status", "ARCHIVED"
        ));
    }

    /**
     * CA-34: Bandeja de Solicitud de Despliegue (Flujo de Aprobación MOCK)
     */
    @PostMapping("/{processDefinitionKey}/request-deploy")
    public ResponseEntity<?> requestDeploymentApproval(
            @PathVariable("processDefinitionKey") String key,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        // El Backend guardaría el XML en BD como PENDIENTE_APROBACION y crearía un UserTask de Camunda para el grupo BPMN_Release_Manager
        return ResponseEntity.ok(Map.of(
            "message", "Solicitud de despliegue enviada. La versión borrador está pendiente de aprobación por Release Management.",
            "status", "PENDING_APPROVAL",
            "assignedGroup", "BPMN_Release_Manager"
        ));
    }

    /**
     * CA-41: Simulador Hardcore Camunda V1 (Instancia y Aborta)
     */
    @SandboxOperation
    @PostMapping("/sandbox-spawn")
    public ResponseEntity<?> sandboxSpawnInstance(@RequestParam("processDefinitionKey") String key) {
        String instanceId = UUID.randomUUID().toString();
        // MOCK Camunda API: 
        // 1. ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "SANDBOX_TEST-" + UUID.randomUUID());
        // 2. runtimeService.deleteProcessInstance(pi.getId(), "SIMULACION_SANDBOX_TERMINADA");
        
        return ResponseEntity.ok(Map.of(
            "message", "Test Sandbox de Camunda superado. El XML parsea exitosamente un token y lo destruye sin afectar datos en vivo.",
            "mockSpawnedId", instanceId,
            "status", "SIMULATION_DESTROYED"
        ));
    }

    /**
     * CA-42: Historial Git-Log Audit
     */
    @GetMapping("/{processDefinitionKey}/audit-logs")
    public ResponseEntity<List<Map<String, String>>> getBpmnAuditLogs(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(List.of(
            Map.of("timestamp", "2023-11-20 10:00:00", "action", "IMPORT XML", "user", "arq-mock-1"),
            Map.of("timestamp", "2023-11-20 10:15:00", "action", "REQUEST DEPLOY", "user", "arq-mock-1"),
            Map.of("timestamp", "2023-12-05 08:30:00", "action", "ARCHIVED", "user", "sys-admin-role")
        ));
    }

    /**
     * CA-50: Diccionario de Variables Zod MOCK
     */
    @GetMapping("/{processDefinitionKey}/variables")
    public ResponseEntity<List<Map<String, String>>> getProcessVariables(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(List.of(
            Map.of("name", "cliente_email", "type", "String"),
            Map.of("name", "monto", "type", "Number"),
            Map.of("name", "aprobado", "type", "Boolean")
        ));
    }
}
