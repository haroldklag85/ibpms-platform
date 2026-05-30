// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.TaskDraftService;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller exposing the US-029 CQRS contracts for US-003 Integration (CA-91).
 */
@RestController
@RequestMapping("/api/v1")
@Traceability(US = "US-003", CA = {"CA-91"})
public class TaskDraftApiController {

    private final TaskDraftService taskDraftService;
    private final Bucket draftRateLimiterBucket;

    public TaskDraftApiController(TaskDraftService taskDraftService, Bucket draftRateLimiterBucket) {
        this.taskDraftService = taskDraftService;
        this.draftRateLimiterBucket = draftRateLimiterBucket;
    }

    /**
     * POST /api/v1/drafts/{taskId}
     * Saves a draft payload.
     */
    @PostMapping("/drafts/{taskId}")
    public ResponseEntity<?> saveDraft(@PathVariable UUID taskId, @RequestBody Map<String, Object> payload) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Persistir borrador parcial
        String username = SecurityContextHolder.getContext().getAuthentication() != null 
                ? SecurityContextHolder.getContext().getAuthentication().getName() 
                : "system";
        taskDraftService.saveDraft(taskId, payload, username);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/drafts/{taskId}
     * Retrieves the saved draft payload.
     */
    @GetMapping("/drafts/{taskId}")
    public ResponseEntity<?> getDraft(@PathVariable UUID taskId) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Reconstruir borrador al reabrir tarea
        Map<String, Object> draft = taskDraftService.getDraft(taskId);
        return ResponseEntity.ok(draft);
    }

    /**
     * DELETE /api/v1/drafts/{taskId}
     * Purges draft post-submit.
     */
    @DeleteMapping("/drafts/{taskId}")
    public ResponseEntity<?> deleteDraft(@PathVariable UUID taskId) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Purgar borrador post-submit
        taskDraftService.deleteDraft(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tasks/{taskId}/complete
     * Completes a task.
     */
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable UUID taskId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-72: Completar tarea con I/O Mapping e If-Match
        String username = SecurityContextHolder.getContext().getAuthentication() != null 
                ? SecurityContextHolder.getContext().getAuthentication().getName() 
                : "system";
        
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload.containsKey("variables") 
                ? (Map<String, Object>) payload.get("variables") 
                : payload;

        taskDraftService.completeTask(taskId, variables, username, ifMatch);
        return ResponseEntity.noContent().build();
    }
}
