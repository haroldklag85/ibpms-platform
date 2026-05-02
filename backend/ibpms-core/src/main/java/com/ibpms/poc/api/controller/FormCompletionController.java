package com.ibpms.poc.api.controller;

import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.application.dto.FormSubmitResponse;
import com.ibpms.poc.application.service.FormCompletionService;
import com.ibpms.poc.application.service.SagaCompensationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workbox/bpmn-tasks")
public class FormCompletionController {

    private final FormCompletionService formCompletionService;

    public FormCompletionController(FormCompletionService formCompletionService) {
        this.formCompletionService = formCompletionService;
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<FormSubmitResponse> completeTask(
            @PathVariable String taskId,
            @Valid @RequestBody FormSubmitRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName(); // Obtenemos del JWT SecurityContext
        
        FormSubmitResponse response = formCompletionService.completeTask(taskId, request, userId);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(SagaCompensationException.class)
    public ResponseEntity<FormSubmitResponse> handleSagaCompensation(SagaCompensationException ex) {
        // Enforce the API gateway contract: Return HTTP 500 with payload SAGA_COMPENSATION_EXECUTED
        FormSubmitResponse response = FormSubmitResponse.builder()
                .status("SAGA_COMPENSATION_EXECUTED")
                .message("Process engine failed to confirm completion. Rolling back.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
