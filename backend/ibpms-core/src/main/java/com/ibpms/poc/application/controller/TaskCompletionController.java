package com.ibpms.poc.application.controller;

import com.ibpms.poc.application.service.FormSubmissionUseCase;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for executing user task completions.
 * Connects the web layer with the {@link FormSubmissionUseCase} to process forms.
 */
@RestController
@RequestMapping("/api/v1/workbox/tasks")
@RequiredArgsConstructor
@Traceability(US = "US-017", CA = {"CA-01", "CA-15"})
public class TaskCompletionController {

    private final FormSubmissionUseCase formSubmissionUseCase;

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        // Extract userId if possible. For now, use "system" or extract from security context if available.
        // Assuming a dummy userId for now to fulfill the entity constraint if not present.
        String userId = "system"; // Alternatively, could inject Principal or Jwt
        
        String eventReference = formSubmissionUseCase.submitForm(id, payload, userId);
        return ResponseEntity.ok(Map.of("eventReference", eventReference));
    }
}
