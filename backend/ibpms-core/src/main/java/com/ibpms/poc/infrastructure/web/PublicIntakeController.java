package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Public Intake Tracking (Integration Gaps Mock).
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicIntakeController {

    @GetMapping("/tracking/{trackingCode}")
    public ResponseEntity<Map<String, Object>> getTrackingInfo(@PathVariable("trackingCode") String trackingCode) {
        // Mock Implementation for Public Tracking (No JWT required)
        return ResponseEntity.ok(Map.of(
                "trackingCode", trackingCode,
                "status", "IN_PROGRESS",
                "currentStage", "Revisión Documental Radicado",
                "applicantName", "Mocked Applicant"));
    }
}
