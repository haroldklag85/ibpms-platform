package com.ibpms.poc.application.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.rest.dto.GenericFormConfigUpdateRequest;
import com.ibpms.poc.application.service.BpmnDesignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/design/processes")
@Tag(name = "Process Design Controller", description = "Endpoints for process design configuration (CA-7)")
public class ProcessDesignController {

    private final BpmnDesignService bpmnDesignService;
    private final ObjectMapper objectMapper;

    public ProcessDesignController(BpmnDesignService bpmnDesignService, ObjectMapper objectMapper) {
        this.bpmnDesignService = bpmnDesignService;
        this.objectMapper = objectMapper;
    }

    @PutMapping("/{processKey}/generic-form-config")
    @Operation(summary = "Update Generic Form Config", description = "Configures the whitelist in ibpms_bpmn_process_design")
    @PreAuthorize("isAuthenticated()") // Only admins/designers in real scenarios
    public ResponseEntity<Void> updateGenericFormConfig(
            @PathVariable("processKey") String processKey,
            @Valid @RequestBody GenericFormConfigUpdateRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "anonymous";
        String whitelistJson;
        try {
            whitelistJson = objectMapper.writeValueAsString(request.getWhitelist());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }

        bpmnDesignService.updateGenericFormConfig(processKey, whitelistJson, userId);
        return ResponseEntity.noContent().build();
    }
}
