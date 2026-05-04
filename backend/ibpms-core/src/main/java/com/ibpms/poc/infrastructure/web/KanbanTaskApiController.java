package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.MoveKanbanTaskUseCase;
import com.ibpms.poc.application.port.out.KanbanColumnPort;
import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.application.service.KanbanColumnService;
import com.ibpms.poc.domain.model.kanban.KanbanColumn;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kanban-tasks")
public class KanbanTaskApiController {

    private final MoveKanbanTaskUseCase moveKanbanTaskUseCase;
    private final KanbanTaskPort kanbanTaskPort;
    private final KanbanColumnService kanbanColumnService;
    private final KanbanColumnPort kanbanColumnPort;

    public KanbanTaskApiController(MoveKanbanTaskUseCase moveKanbanTaskUseCase, KanbanTaskPort kanbanTaskPort, KanbanColumnService kanbanColumnService, KanbanColumnPort kanbanColumnPort) {
        this.moveKanbanTaskUseCase = moveKanbanTaskUseCase;
        this.kanbanTaskPort = kanbanTaskPort;
        this.kanbanColumnService = kanbanColumnService;
        this.kanbanColumnPort = kanbanColumnPort;
    }

    @PatchMapping("/tasks/{taskId}/state")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> updateTaskState(@PathVariable UUID taskId, @RequestBody Map<String, String> body, Authentication authentication) {
        String newState = body.get("newState");
        String blockedReason = body.get("blockedReason");
        String userId = authentication.getName();

        moveKanbanTaskUseCase.moveTask(taskId, newState, blockedReason, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<List<KanbanTask>> getTasksByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(kanbanTaskPort.findByBoardId(boardId));
    }

    @GetMapping("/boards/{boardId}/columns")
    public ResponseEntity<List<KanbanColumn>> getColumnsByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(kanbanColumnPort.findByBoardId(boardId));
    }

    @PostMapping("/boards/{boardId}/columns")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<KanbanColumn> createColumn(@PathVariable UUID boardId, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        KanbanColumn column = kanbanColumnService.createColumn(boardId, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(column);
    }

    @DeleteMapping("/boards/{boardId}/columns/{colId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteColumn(@PathVariable UUID boardId, @PathVariable UUID colId) {
        kanbanColumnService.deleteColumn(colId);
        return ResponseEntity.noContent().build();
    }
}
