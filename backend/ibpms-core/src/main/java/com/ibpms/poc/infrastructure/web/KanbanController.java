package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kanban")
public class KanbanController {

    private final KanbanBoardRepository boardRepository;
    private final KanbanTaskRepository taskRepository;

    public KanbanController(KanbanBoardRepository boardRepository, KanbanTaskRepository taskRepository) {
        this.boardRepository = boardRepository;
        this.taskRepository = taskRepository;
    }

    // --- Tableros (Boards) ---

    @GetMapping("/boards")
    public ResponseEntity<List<KanbanBoardEntity>> getAllBoards() {
        return ResponseEntity.ok(boardRepository.findAll());
    }

    @PostMapping("/boards")
    public ResponseEntity<KanbanBoardEntity> createBoard(@RequestBody KanbanBoardEntity board) {
        board.setId(UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.CREATED).body(boardRepository.save(board));
    }

    // --- Tarjetas (Tasks) ---

    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<List<KanbanTaskEntity>> getTasksByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(taskRepository.findByBoardIdOrderByUpdatedAtDesc(boardId));
    }

    @PostMapping("/boards/{boardId}/tasks")
    public ResponseEntity<KanbanTaskEntity> createTask(@PathVariable UUID boardId, @RequestBody KanbanTaskEntity task) {
        Optional<KanbanBoardEntity> boardOpt = boardRepository.findById(java.util.Objects.requireNonNull(boardId));
        if (boardOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        task.setId(UUID.randomUUID());
        task.setBoard(boardOpt.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRepository.save(task));
    }

    /**
     * Endpoint crucial para soportar Drag & Drop (Arrastrar y Soltar) en el
     * Frontend.
     * Utiliza PATCH para actualizar solo el atributo "status" de la tarjeta.
     */
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable UUID taskId, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("newStatus");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El campo 'newStatus' es requerido.");
        }

        Optional<KanbanTaskEntity> taskOpt = taskRepository.findById(java.util.Objects.requireNonNull(taskId));
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        KanbanTaskEntity task = taskOpt.get();
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        try {
            KanbanTaskEntity saved = taskRepository.save(task);
            return ResponseEntity.ok(saved);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            // Manejo de concurrencia: Alguien más movió la tarjeta al mismo tiempo
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La tarjeta fue modificada por otro usuario recientemente. Por favor, refresca el tablero (F5).");
        }
    }
}
