package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.KanbanBoardService;
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
@RequestMapping("/api/v1/kanban")
@Traceability(US = "US-008", CA = {"CA-01", "CA-06", "CA-08"})
public class KanbanController {

    private final KanbanBoardService boardService;

    public KanbanController(KanbanBoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/boards")
    public ResponseEntity<List<KanbanBoardEntity>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    @PostMapping("/boards")
    public ResponseEntity<KanbanBoardEntity> createBoard(@RequestBody KanbanBoardEntity board) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(board));
    }

    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<List<KanbanTaskEntity>> getTasksByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(boardService.getTasksByBoard(boardId));
    }

    @PostMapping("/boards/{boardId}/tasks")
    public ResponseEntity<KanbanTaskEntity> createTask(@PathVariable UUID boardId, @RequestBody KanbanTaskEntity task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createTask(boardId, task));
    }

    @PatchMapping("/tasks/{taskId}/move")
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
