package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.ClaimAuditService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.*;

@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*")
@Traceability(US = "US-004", CA = {"CA-01"})
public class TaskClaimApiController {

    private final AgileTaskService taskService;
    private final ClaimAuditService claimAuditService;

    public TaskClaimApiController(AgileTaskService taskService, ClaimAuditService claimAuditService) {
        this.taskService = taskService;
        this.claimAuditService = claimAuditService;
    }

    /**
     * POST /{taskId}/claim
     */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<?> claimTask(@PathVariable UUID taskId) {
        String assignee = SecurityContextUtils.getAssignee();
        taskService.claimTask(taskId, assignee);
        
        claimAuditService.audit(taskId, assignee, "CLAIMED", null, null, null);

        return ResponseEntity.ok(Map.of("message", "Tarea reclamada exitosamente.", "taskId", taskId.toString()));
    }

    /**
     * POST /bulk-claim
     * GAP-004
     */
    @PostMapping("/bulk-claim")
    public ResponseEntity<?> bulkClaim(@RequestBody Map<String, List<String>> body) {
        List<String> taskIds = body.get("taskIds");
        if (taskIds == null || taskIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "taskIds is required"));
        }
        if (taskIds.size() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Hard limit of 20 tasks exceeded"));
        }

        String assignee = SecurityContextUtils.getAssignee();
        return ResponseEntity.ok(taskService.bulkClaim(taskIds, assignee));
    }

    /**
     * POST /{taskId}/release
     * GAP-005
     */
    @PostMapping("/{taskId}/release")
    public ResponseEntity<?> releaseTask(@PathVariable UUID taskId, @RequestBody(required = false) Map<String, String> body) {
        String message = (body != null) ? body.get("message") : null;
        if (message != null && message.length() > 500) {
            message = message.substring(0, 500);
        }

        String assignee = SecurityContextUtils.getAssignee();
        taskService.releaseTask(taskId, assignee, message);

        return ResponseEntity.ok(Map.of(
                "taskId", taskId.toString(),
                "releasedAt", java.time.Instant.now().toString()
        ));
    }

    /**
     * POST /{taskId}/force-unclaim
     * GAP-006
     */
    @PostMapping("/{taskId}/force-unclaim")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<?> forceUnclaimTask(@PathVariable UUID taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String supervisorId = auth.getName();
        String supervisorTeamId = null;
        
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            supervisorTeamId = jwtToken.getToken().getClaimAsString("team_id");
        }

        Map<String, String> result = taskService.forceUnclaimWithValidation(taskId, supervisorId, supervisorTeamId);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /{taskId}/extend-timeout
     */
    @PostMapping("/{taskId}/extend-timeout")
    public ResponseEntity<?> extendTimeout(@PathVariable UUID taskId) {
        String assignee = SecurityContextUtils.getAssignee();
        taskService.extendTimeout(taskId, assignee);
        return ResponseEntity.ok(Map.of("message", "Timeout extended"));
    }

    /**
     * GET /{taskId}/audit-trail
     * GAP-007
     */
    @GetMapping("/{taskId}/audit-trail")
    public ResponseEntity<?> getAuditTrail(@PathVariable UUID taskId) {
        List<ClaimAuditLog> trail = claimAuditService.getAuditTrail(taskId);
        
        List<Map<String, Object>> entries = new ArrayList<>();
        for (ClaimAuditLog log : trail) {
            Map<String, Object> map = new HashMap<>();
            map.put("action", log.getActionType());
            map.put("userId", log.getUserId());
            map.put("userName", log.getUserId()); // Simulado
            map.put("timestamp", log.getTimestamp());
            map.put("reason", log.getReason());
            map.put("message", log.getMessage());
            entries.add(map);
        }
        
        return ResponseEntity.ok(Map.of("entries", entries));
    }
}
