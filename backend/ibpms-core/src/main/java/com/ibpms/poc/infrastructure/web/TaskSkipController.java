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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
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

    public record TaskSkipRequest(
            @NotNull(message = "El motivo (reason) es requerido") SkipReason reason,
            String details
    ) {}
}
