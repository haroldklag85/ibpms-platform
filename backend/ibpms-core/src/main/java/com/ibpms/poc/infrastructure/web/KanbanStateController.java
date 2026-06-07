package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.KanbanBoardService;
import com.ibpms.poc.application.service.KanbanStateMachine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.application.util.SecurityContextUtils;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kanban")
@Traceability(US = "US-008", CA = {"CA-06"})
public class KanbanStateController {

    private final KanbanBoardService boardService;
    private final KanbanStateMachine stateMachine;

    public KanbanStateController(KanbanBoardService boardService, KanbanStateMachine stateMachine) {
        this.boardService = boardService;
        this.stateMachine = stateMachine;
    }

    @PatchMapping("/{taskId}/state")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> updateState(@PathVariable String taskId, @Valid @RequestBody UpdateStateRequest request) {
        boardService.updateTaskState(taskId, request.newState(), stateMachine);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getBoard(@RequestParam java.util.UUID boardId) {
        String tenantId = SecurityContextUtils.getTenantId();
        // Llama al BoardService para retornar las tareas agrupadas por estado (columnas) para el tenant
        Map<String, List<Map<String, Object>>> columns = boardService.getBoardColumns(tenantId, boardId);
        return ResponseEntity.ok(columns);
    }

    public record UpdateStateRequest(
            @NotBlank(message = "El nuevo estado es requerido") String newState
    ) {}
}
