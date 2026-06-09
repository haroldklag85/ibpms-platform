package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Project Builder operations (Mock implementation).
 */
@RestController
@RequestMapping("/api/v1/projects")
@Traceability(US = "US-006", CA = {"CA-01"})
public class ProjectBuilderController {

    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createProjectTemplate(@RequestBody Map<String, Object> request) {
        // Mock Implementation
        return ResponseEntity.ok(Map.of(
                "templateId", UUID.randomUUID().toString(),
                "name", request.getOrDefault("name", "Mocked Template"),
                "status", "DRAFT_SAVED",
                "nodesCount", 5));
    }
}
