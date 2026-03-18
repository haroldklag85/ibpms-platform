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
/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/design/processes")
public class BpmnDesignController {

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
    public ResponseEntity<?> deployBpmnProcess(@RequestParam("file") MultipartFile file) {
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
}
