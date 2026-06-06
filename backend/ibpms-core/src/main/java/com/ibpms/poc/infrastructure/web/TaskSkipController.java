package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.SkipAuditService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.agile.SkipReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agile/tasks")
@Traceability(US = "US-004", CA = {"CA-01"})
public class TaskSkipController {

    private final SkipAuditService skipAuditService;

    public TaskSkipController(SkipAuditService skipAuditService) {
        this.skipAuditService = skipAuditService;
    }

    @PostMapping("/{taskId}/skip")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<AgileTask> skipTask(@PathVariable UUID taskId, @Valid @RequestBody TaskSkipRequest request) {
        String username = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();

        AgileTask nextTask = skipAuditService.skipTaskAndGetNext(taskId, request.reason(), request.details(), username, tenantId);

        if (nextTask == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(nextTask);
    }

    @GetMapping("/skip-audit")
    public ResponseEntity<java.util.List<com.ibpms.poc.infrastructure.jpa.entity.SkipAuditEntity>> getSkipAuditLogs() {
        // @Traceability: US-008 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(skipAuditService.findAll());
    }

    public record TaskSkipRequest(
            @NotNull(message = "El motivo (reason) es requerido") SkipReason reason,
            String details
    ) {}
}
