package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller de Administración de Incidentes BPMN (CA-13).
 * Provee herramientas operativas para gestión de fallos en runtime y DRP.
 */
@RestController
@RequestMapping("/api/v1/admin/incidents")
public class IncidentController {

    /**
     * CA-13: Obtener lista paginada de incidentes huérfanos o fallidos.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        // MOCK Camunda API: runtimeService.createIncidentQuery().list()
        List<Map<String, Object>> mockIncidents = List.of(
            Map.of("id", "inc-001", "processInstanceId", "pi-123", "incidentType", "failedJob", "incidentMessage", "Timeout al invocar REST API"),
            Map.of("id", "inc-002", "processInstanceId", "pi-987", "incidentType", "failedExternalTask", "incidentMessage", "Worker desconectado")
        );
        return ResponseEntity.ok(mockIncidents);
    }

    /**
     * CA-13: Re-intento forzoso de Job asíncrono atascado.
     */
    @PostMapping("/{jobId}/retry")
    public ResponseEntity<Map<String, String>> retryJob(@PathVariable("jobId") String jobId) {
        // MOCK Camunda API: managementService.executeJob(jobId);
        return ResponseEntity.ok(Map.of(
            "message", "Re-intento de job encolado exitosamente.",
            "jobId", jobId,
            "status", "RETRYING"
        ));
    }

    /**
     * CA-13: Anulación Forense / Abortaje de Instancia.
     */
    @DeleteMapping("/instances/{instanceId}")
    public ResponseEntity<Map<String, String>> abortProcessInstance(@PathVariable("instanceId") String instanceId) {
        // MOCK Camunda API: runtimeService.deleteProcessInstance(instanceId, "Terminado administrativamente (CA-13)");
        return ResponseEntity.ok(Map.of(
            "message", "Instancia anulada tajantemente del Engine.",
            "instanceId", instanceId,
            "status", "DELETED"
        ));
    }
}
