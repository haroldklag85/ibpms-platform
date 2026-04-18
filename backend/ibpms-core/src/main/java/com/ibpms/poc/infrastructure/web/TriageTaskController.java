package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.TriageTaskService;
import com.ibpms.poc.domain.model.TriageTask;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intake/triage/tasks")
public class TriageTaskController {

    private final TriageTaskService triageTaskService;

    public TriageTaskController(TriageTaskService triageTaskService) {
        this.triageTaskService = triageTaskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Page<TriageTask>> getTriageTasks(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            Pageable pageable) {
        return ResponseEntity.ok(triageTaskService.listTasks(status, pageable));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<TriageTask> approveTask(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveTriageRequest request) {
        return ResponseEntity.ok(triageTaskService.approveTask(id, request.processType()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<TriageTask> rejectTask(
            @PathVariable UUID id,
            @Valid @RequestBody RejectTriageRequest request) {
        return ResponseEntity.ok(triageTaskService.rejectTask(id, request.rejectionReason()));
    }

    public record ApproveTriageRequest(
            @NotBlank(message = "El processType es obligatorio") String processType
    ) {}

    public record RejectTriageRequest(
            @NotBlank(message = "El rejectionReason es obligatorio") String rejectionReason
    ) {}
}
