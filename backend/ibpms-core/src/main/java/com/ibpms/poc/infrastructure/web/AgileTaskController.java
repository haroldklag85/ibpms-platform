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
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponse> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateTaskRequest request,
        Authentication authentication) {
        String createdBy = authentication.getName();
        AgileTask task = taskService.createTask(
            projectId, request.title(), request.description(), request.effort(), request.assigneeIds(), request.tags(), request.notes(), createdBy);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Page<TaskResponse>> listTasks(
        @PathVariable UUID projectId,
        @RequestParam(required = false, defaultValue = "false") boolean includeCompleted,
        Pageable pageable) {
        Page<AgileTask> page = taskService.listTasks(projectId, includeCompleted, pageable);
        return ResponseEntity.ok(page.map(TaskResponse::from));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponse> getTaskDetail(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId) {
        return ResponseEntity.ok(TaskResponse.from(taskService.getTask(taskId)));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateTaskRequest request,
        Authentication authentication) {
        String updatedBy = authentication.getName();
        AgileTask task = taskService.updateTask(
            taskId, request.title(), request.description(), request.effort(), request.status(), request.slaDeadline(), request.assigneeIds(), request.tags(), request.notes(), updatedBy);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    // CA-4: Eliminar con auditoría forense (Soft delete -> Hard delete)
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        Authentication authentication) {
        String deletedBy = authentication.getName();
        taskService.deleteTask(taskId, deletedBy);
        return ResponseEntity.noContent().build();
    }

    // CA-6: Reordenar por drag & drop
    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> reorderTasks(
        @PathVariable UUID projectId,
        @Valid @RequestBody ReorderRequest request) {
        taskService.reorderTasks(projectId, request.orderedTaskIds());
        return ResponseEntity.noContent().build();
    }

    // CA-5 + CA-14: Asignación masiva interactiva
    @PatchMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
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
        BigDecimal effort,
        java.util.Set<String> assigneeIds,
        java.util.Set<String> tags,
        String notes
    ) {}

    public record UpdateTaskRequest(
        String title,
        String description,
        BigDecimal effort,
        String status,
        java.time.ZonedDateTime slaDeadline,
        java.util.Set<String> assigneeIds,
        java.util.Set<String> tags,
        String notes
    ) {}

    public record ReorderRequest(
        @NotEmpty(message = "Se requiere la lista ordenada") List<UUID> orderedTaskIds
    ) {}

    public record BulkAssignRequest(
        @NotEmpty(message = "Debe enviar tareas") List<UUID> taskIds,
        @NotBlank(message = "El usuario asignado es obligatorio") String userId
    ) {}

    public record TaskResponse(
            UUID id,
            UUID projectId,
            String title,
            String description,
            BigDecimal effortEstimated,
            String status,
            Integer position,
            java.time.ZonedDateTime slaDeadline,
            java.time.ZonedDateTime lastActivityAt,
            String createdBy,
            java.util.Set<String> assigneeIds,
            java.util.Set<String> tags,
            String notes,
            long daysInactive
    ) {
        public static TaskResponse from(AgileTask task) {
            long days = 0;
            if (task.getLastActivityAt() != null) {
                days = java.time.temporal.ChronoUnit.DAYS.between(task.getLastActivityAt(), java.time.ZonedDateTime.now());
            }
            return new TaskResponse(
                    task.getId(),
                    task.getProjectId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getEffortEstimated(),
                    task.getStatus(),
                    task.getPosition(),
                    task.getSlaDeadline(),
                    task.getLastActivityAt(),
                    task.getCreatedBy(),
                    task.getAssigneeIds(),
                    task.getTags(),
                    task.getNotes(),
                    days
            );
        }
    }
}
