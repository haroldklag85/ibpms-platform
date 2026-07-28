package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.KanbanBoardService;
import com.ibpms.poc.application.dto.KanbanBoardDto;
import com.ibpms.poc.application.dto.KanbanTaskStateDto;
import com.ibpms.poc.application.dto.KanbanStateUpdatePayload;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Traceability(US = "US-008", CA = {"CA-01", "CA-06", "CA-08", "CA-12"})
public class KanbanController {

    private final KanbanBoardService boardService;

    public KanbanController(KanbanBoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/api/v1/kanban/boards")
    public ResponseEntity<List<KanbanBoardEntity>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    @PostMapping("/api/v1/kanban/boards")
    public ResponseEntity<KanbanBoardEntity> createBoard(@RequestBody KanbanBoardEntity board) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(board));
    }

    @GetMapping("/api/v1/projects/{projectId}/kanban")
    public ResponseEntity<KanbanBoardDto> getKanbanBoard(
            @PathVariable String projectId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        return ResponseEntity.ok(boardService.getKanbanBoard(tenantId, projectId));
    }

    @PatchMapping("/api/v1/projects/{projectId}/kanban/tasks/{taskId}/state")
    public ResponseEntity<?> updateTaskState(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody KanbanStateUpdatePayload payload) {
        
        try {
            KanbanTaskStateDto saved = boardService.moveTask(projectId, taskId, payload.getNewStatus(), payload.getAssignee());
            return ResponseEntity.ok(saved);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La tarjeta fue modificada por otro usuario recientemente. Por favor, refresca el tablero.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ENDPOINTS PORTADOS DE SPRINT-6 (Board CRUD Legacy)
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/api/v1/kanban/boards/{boardId}/tasks")
    public ResponseEntity<List<KanbanTaskEntity>> getTasksByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(boardService.getTasksByBoard(boardId));
    }

    @PostMapping("/api/v1/kanban/boards/{boardId}/tasks")
    public ResponseEntity<KanbanTaskEntity> createTask(@PathVariable UUID boardId, @RequestBody KanbanTaskEntity task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createTask(boardId, task));
    }

    @PatchMapping("/api/v1/kanban/tasks/{taskId}/move")
    public ResponseEntity<?> moveTask(@PathVariable UUID taskId, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("newStatus");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El campo 'newStatus' es requerido.");
        }
        if ("BLOCKED".equalsIgnoreCase(newStatus)) {
            String reason = payload.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El campo 'reason' es obligatorio cuando se mueve a estado BLOCKED.");
            }
        }

        try {
            KanbanTaskEntity saved = boardService.moveTaskLegacy(taskId, newStatus);
            return ResponseEntity.ok(saved);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La tarjeta fue modificada por otro usuario recientemente. Por favor, refresca el tablero (F5).");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
