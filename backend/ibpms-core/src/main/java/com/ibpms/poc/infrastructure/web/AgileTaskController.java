package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.domain.model.agile.AgileTask;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agile/projects/{projectId}/tasks")
public class AgileTaskController {

    private final AgileTaskService taskService;

    public AgileTaskController(AgileTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<AgileTask> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateTaskRequest request) {
        String createdBy = "admin";
        return ResponseEntity.ok(taskService.createTask(
            projectId, request.title(), request.description(), request.effort(), createdBy));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Page<AgileTask>> listTasks(
        @PathVariable UUID projectId,
        @RequestParam(required = false, defaultValue = "false") boolean showCompleted,
        Pageable pageable) {
        return ResponseEntity.ok(taskService.listTasks(projectId, showCompleted, pageable));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<AgileTask> getTaskDetail(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

    @PatchMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<AgileTask> updateTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateTaskRequest request) {
        String updatedBy = "admin"; // Simulado
        return ResponseEntity.ok(taskService.updateTask(
            taskId, request.title(), request.description(), request.effort(), request.status(), request.slaDeadline(), updatedBy));
    }

    // CA-4: Eliminar con auditoría forense (Soft delete)
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId) {
        String deletedBy = "admin"; // Simulado
        taskService.deleteTask(taskId, deletedBy);
        return ResponseEntity.noContent().build();
    }

    // CA-6: Reordenar por drag & drop
    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> reorderTasks(
        @PathVariable UUID projectId,
        @Valid @RequestBody ReorderRequest request) {
        taskService.reorderTasks(projectId, request.orderedTaskIds());
        return ResponseEntity.noContent().build();
    }

    // CA-5 + CA-14: Asignación masiva interactiva
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> bulkAssign(
        @PathVariable UUID projectId,
        @Valid @RequestBody BulkAssignRequest request) {
        taskService.bulkAssign(projectId, request.taskIds(), request.userId());
        return ResponseEntity.noContent().build();
    }

    // ========== DTOs ==========

    public record CreateTaskRequest(
        @NotBlank(message = "El título es obligatorio") String title,
        String description,
        BigDecimal effort
    ) {}

    public record UpdateTaskRequest(
        String title,
        String description,
        BigDecimal effort,
        String status,
        java.time.ZonedDateTime slaDeadline
    ) {}

    public record ReorderRequest(
        @NotEmpty(message = "Se requiere la lista ordenada") List<UUID> orderedTaskIds
    ) {}

    public record BulkAssignRequest(
        @NotEmpty(message = "Debe enviar tareas") List<UUID> taskIds,
        @NotBlank(message = "El usuario asignado es obligatorio") String userId
    ) {}
}
