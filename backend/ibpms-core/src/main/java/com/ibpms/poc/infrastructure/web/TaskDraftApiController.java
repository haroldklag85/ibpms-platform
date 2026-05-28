package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.TaskDraftService;
import com.ibpms.poc.crosscutting.annotations.Traceability;
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

    public TaskDraftApiController(TaskDraftService taskDraftService) {
        this.taskDraftService = taskDraftService;
    }

    /**
     * POST /api/v1/drafts/{taskId}
     * Saves a draft payload.
     */
    @PostMapping("/drafts/{taskId}")
    public ResponseEntity<Void> saveDraft(@PathVariable UUID taskId, @RequestBody Map<String, Object> payload) {
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
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable UUID taskId) {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Reconstruir borrador al reabrir tarea
        Map<String, Object> draft = taskDraftService.getDraft(taskId);
        return ResponseEntity.ok(draft);
    }

    /**
     * DELETE /api/v1/drafts/{taskId}
     * Purges draft post-submit.
     */
    @DeleteMapping("/drafts/{taskId}")
    public ResponseEntity<Void> deleteDraft(@PathVariable UUID taskId) {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Purgar borrador post-submit
        taskDraftService.deleteDraft(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tasks/{taskId}/complete
     * Completes a task.
     */
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable UUID taskId, @RequestBody Map<String, Object> payload) {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-91: Completar tarea con I/O Mapping
        String username = SecurityContextHolder.getContext().getAuthentication() != null 
                ? SecurityContextHolder.getContext().getAuthentication().getName() 
                : "system";
        
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload.containsKey("variables") 
                ? (Map<String, Object>) payload.get("variables") 
                : payload;

        taskDraftService.completeTask(taskId, variables, username);
        return ResponseEntity.noContent().build();
    }
}
