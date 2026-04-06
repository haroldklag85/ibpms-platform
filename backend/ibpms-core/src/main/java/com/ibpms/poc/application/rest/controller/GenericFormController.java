package com.ibpms.poc.application.rest.controller;

import com.ibpms.poc.application.rest.dto.GenericFormContextResponse;
import com.ibpms.poc.application.rest.dto.GenericFormSubmitRequest;
import com.ibpms.poc.application.service.GenericFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/workbox/tasks")
@Tag(name = "Generic Form Controller", description = "Endpoints for generic forms (Pantalla 7.B - US-039)")
public class GenericFormController {

    private final GenericFormService genericFormService;

    public GenericFormController(GenericFormService genericFormService) {
        this.genericFormService = genericFormService;
    }

    @GetMapping("/{id}/generic-form-context")
    @Operation(summary = "Get Generic Form Context", description = "Retrieves schema, allowed results, and prefill data using whitelist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericFormContextResponse> getGenericFormContext(@PathVariable("id") String taskId) {
        GenericFormContextResponse response = genericFormService.getGenericFormContext(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/generic-form-complete")
    @Operation(summary = "Complete Task via Generic Form", description = "Submits a generic form and routes execution using optional panic actions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> submitGenericForm(
            @PathVariable("id") String taskId,
            @Valid @RequestBody GenericFormSubmitRequest request,
            Authentication authentication) {
        
        // El nombre de usuario que hace el submit, derivado del JWT / Contexto de Seguridad
        String userId = authentication != null ? authentication.getName() : "anonymous";
        
        genericFormService.submitGenericForm(taskId, request, userId);
        return ResponseEntity.noContent().build();
    }
}
