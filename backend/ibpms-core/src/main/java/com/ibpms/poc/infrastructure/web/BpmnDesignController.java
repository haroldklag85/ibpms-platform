package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.service.PreFlightAnalyzerService;

import java.util.Map;

/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/design/processes")
public class BpmnDesignController {

    private final PreFlightAnalyzerService preFlightAnalyzerService;

    public BpmnDesignController(PreFlightAnalyzerService preFlightAnalyzerService) {
        this.preFlightAnalyzerService = preFlightAnalyzerService;
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
                "warnings", validation.getWarnings()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fallo al procesar el archivo BPMN: " + e.getMessage()));
        }
    }
}
