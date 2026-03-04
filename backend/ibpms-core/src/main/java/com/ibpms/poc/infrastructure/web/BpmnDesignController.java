package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for BPMN Design operations (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/design/processes")
public class BpmnDesignController {

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
}
