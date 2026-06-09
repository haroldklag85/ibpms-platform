package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.TaskExecutionService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@Traceability(US = "US-004", CA = {"CA-01"})
public class TaskExecutionController {

    private final TaskExecutionService taskExecutionService;

    public TaskExecutionController(TaskExecutionService taskExecutionService) {
        this.taskExecutionService = taskExecutionService;
    }

    @PostMapping("/{taskId}/skip")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> skipTask(@PathVariable String taskId, @RequestBody Map<String, String> payload) {
        String username = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();
        
        String reason = payload.get("reason");
        String details = payload.get("details");
        
        taskExecutionService.skipTask(taskId, reason, details, username, tenantId);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/force-route")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> forceRouteTask(@PathVariable String taskId, @RequestBody Map<String, String> payload) {
        String username = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();
        
        String targetUserId = payload.get("newUserId");
        if (targetUserId == null) {
            targetUserId = payload.get("targetUserId"); // fallback
        }
        String reason = payload.get("reason");
        
        taskExecutionService.forceRoute(taskId, targetUserId, reason, username, tenantId);
        
        return ResponseEntity.noContent().build();
    }
}
