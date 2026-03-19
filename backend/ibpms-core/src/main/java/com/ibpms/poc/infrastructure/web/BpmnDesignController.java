package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/design/processes")
public class BpmnDesignController {

    private static final Map<String, String> pessimisticLocks = new ConcurrentHashMap<>();

    private final PreFlightAnalyzerService preFlightAnalyzerService;
    private final ProcessMigrationService processMigrationService;

    public BpmnDesignController(PreFlightAnalyzerService preFlightAnalyzerService, 
                                ProcessMigrationService processMigrationService) {
        this.preFlightAnalyzerService = preFlightAnalyzerService;
        this.processMigrationService = processMigrationService;
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

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".bpmn")) {
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
     * CA-16: Bloqueo Pesimista (Adquisición)
     */
    @PostMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> acquireLock(@PathVariable("processDefinitionKey") String key) {
        String mockUser = "user-mock-123";
        if (pessimisticLocks.containsKey(key) && !pessimisticLocks.get(key).equals(mockUser)) {
            return ResponseEntity.status(423).body(Map.of("error", "El proceso ya se encuentra bloqueado por otro usuario."));
        }
        pessimisticLocks.put(key, mockUser);
        return ResponseEntity.ok(Map.of("status", "LOCKED", "owner", mockUser));
    }

    /**
     * CA-16: Bloqueo Pesimista (Liberación)
     */
    @DeleteMapping("/{processDefinitionKey}/lock")
    public ResponseEntity<?> releaseLock(@PathVariable("processDefinitionKey") String key) {
        pessimisticLocks.remove(key);
        return ResponseEntity.ok(Map.of("status", "UNLOCKED"));
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
     * CA-19: Autosave Borrador (POST explícito)
     */
    @PostMapping("/{processDefinitionKey}/draft")
    public ResponseEntity<?> saveDraft(@PathVariable("processDefinitionKey") String key) {
        return ResponseEntity.ok(Map.of("status", "DRAFT_SAVED", "processId", key));
    }

    /**
     * CA-20: Sandbox Simulator (Extrae estáticamente 3 nodos a animar)
     */
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
}
