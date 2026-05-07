package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AgileTaskService {

    private final AgileTaskRepositoryJpa taskRepository;
    private final SlaChangeLogService slaChangeLogService;
    private final AuditLogService auditLogService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository taskAuditLogRepository;
    private final FormFieldCleanserService formFieldCleanserService;
    private final ClaimAuditService claimAuditService;

    public AgileTaskService(AgileTaskRepositoryJpa taskRepository, 
                            SlaChangeLogService slaChangeLogService, 
                            AuditLogService auditLogService,
                            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                            com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository taskAuditLogRepository,
                            FormFieldCleanserService formFieldCleanserService,
                            ClaimAuditService claimAuditService) {
        this.taskRepository = taskRepository;
        this.slaChangeLogService = slaChangeLogService;
        this.auditLogService = auditLogService;
        this.messagingTemplate = messagingTemplate;
        this.taskAuditLogRepository = taskAuditLogRepository;
        this.formFieldCleanserService = formFieldCleanserService;
        this.claimAuditService = claimAuditService;
    }

    @Transactional
    public AgileTask createTask(UUID projectId, String title, String description, BigDecimal effort, java.util.Set<String> assigneeIds, java.util.Set<String> tags, String notes, String createdBy) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título obligatorio");
        }
        
        long activeCount = taskRepository.countByProjectIdAndStatusNotIn(projectId, java.util.List.of("DONE", "DELETED"));
        if (activeCount >= 500) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Límite de 500 tareas activas superado para este proyecto");
        }

        String safeDescription = formFieldCleanserService.sanitizeHtml(description);
        String safeNotes = formFieldCleanserService.sanitizeHtml(notes);

        AgileTask task = AgileTask.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .title(title)
                .description(safeDescription)
                .notes(safeNotes)
                .effortEstimated(effort)
                .assigneeIds(assigneeIds)
                .tags(tags)
                .createdBy(createdBy)
                .build();

        return taskRepository.save(task);
    }

    public Page<AgileTask> listTasks(UUID projectId, boolean includeCompleted, Pageable pageable) {
        if (includeCompleted) {
            return taskRepository.findByProjectIdAndStatusNot(projectId, "DELETED", pageable);
        } else {
            return taskRepository.findByProjectIdAndStatusNotIn(projectId, java.util.List.of("DELETED", "DONE"), pageable);
        }
    }

    public List<AgileTask> getPortfolio(String owner) {
        return taskRepository.findPortfolioByOwner(owner);
    }

    public AgileTask getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public AgileTask getTaskForUpdate(UUID taskId) {
        return taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found (or locked)"));
    }

    @Transactional
    @com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-008", CA = {"CA-02"})
    public AgileTask updateTask(UUID taskId, String title, String description, BigDecimal effort, String status, java.time.ZonedDateTime slaDeadline, java.util.Set<String> assigneeIds, java.util.Set<String> tags, String notes, String updatedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        
        if ("DONE".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CA-02 Violación de Inmutabilidad: Una tarea en estado DONE es estrictamente de solo lectura (Historial forense sellado).");
        }
        
        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(formFieldCleanserService.sanitizeHtml(description));
        if (notes != null) task.setNotes(formFieldCleanserService.sanitizeHtml(notes));
        if (effort != null) task.setEffortEstimated(effort);
        if (status != null) task.setStatus(status);
        if (assigneeIds != null) task.setAssigneeIds(assigneeIds);
        if (tags != null) task.setTags(tags);
        
        if (slaDeadline != null && !slaDeadline.equals(task.getSlaDeadline())) {
            slaChangeLogService.logSlaModification(taskId, task.getSlaDeadline(), slaDeadline, updatedBy);
            task.setSlaDeadline(slaDeadline);
        }
        
        return taskRepository.save(task);
    }

    /**
     * CA-4: Hard delete con auditoría forense antes de borrado.
     */
    @Transactional
    public void deleteTask(UUID taskId, String deletedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        
        if ("DONE".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CA-02 Violación de Inmutabilidad: No se permite eliminar una tarea en estado DONE (Historial forense sellado).");
        }
        
        // GAP 1: Auditoría inmutable antes de borrado (taskId, title, deletedBy, deletedAt implícito en entity)
        com.ibpms.poc.infrastructure.jpa.entity.TaskAuditLogEntity auditLog = 
            new com.ibpms.poc.infrastructure.jpa.entity.TaskAuditLogEntity(
                task.getId().toString(),
                "HARD_DELETE",
                deletedBy,
                null,
                "Deleted task title: " + task.getTitle()
            );
        taskAuditLogRepository.save(auditLog);

        taskRepository.delete(task);
    }

    /**
     * CA-6: Reordenar por drag & drop
     */
    @Transactional
    public void reorderTasks(UUID projectId, List<UUID> orderedTaskIds) {
        for (int i = 0; i < orderedTaskIds.size(); i++) {
            taskRepository.updatePosition(orderedTaskIds.get(i), i);
        }
    }

    /**
     * CA-5 + CA-14: Asignación masiva interactiva
     */
    @Transactional
    public void bulkAssign(UUID projectId, List<UUID> taskIds, String userId) {
        // Ordenamiento lexicográfico para prevenir deadlocks en concurrencia masiva
        List<UUID> sortedTaskIds = taskIds.stream().sorted().toList();
        for (UUID taskId : sortedTaskIds) {
            try {
                AgileTask task = getTaskForUpdate(taskId);
                if ("DONE".equals(task.getStatus())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CA-02 Violación de Inmutabilidad: No se permite re-asignar una tarea en estado DONE.");
                }
                if (task.getAssigneeIds() == null) {
                    task.setAssigneeIds(new java.util.HashSet<>());
                }
                task.getAssigneeIds().add(userId);
                taskRepository.save(task);
            } catch (org.springframework.dao.OptimisticLockingFailureException | org.springframework.dao.CannotAcquireLockException e) {
                // Logueo defensivo en caso de pisada de datos o lock timeout
                System.err.println("[DEFENSIVO] Conflicto transaccional al asignar tarea " + taskId + ": " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflicto de concurrencia en la asignación de tareas. Reintente.", e);
            }
        }
    }

    /**
     * @Traceability: US-002 - CA-1
     * Reclama una tarea y emite un evento WebSocket a la UI.
     */
    @Transactional
    public void claimTask(UUID taskId, String claimedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        if (!"OPEN".equals(task.getStatus()) && !"AVAILABLE".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea no está disponible para reclamación");
        }
        task.setStatus("CLAIMED");
        if (task.getAssigneeIds() == null) {
            task.setAssigneeIds(new java.util.HashSet<>());
        }
        if (!task.getAssigneeIds().contains(claimedBy)) {
            task.getAssigneeIds().add(claimedBy);
        }
        taskRepository.save(task);

        // US-002 CA-13 + CA-27: Emisión STOMP WebSocket Tipada
        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", com.ibpms.poc.domain.model.agile.WebSocketEventType.TASK_CLAIMED.name(),
                "taskId", taskId,
                "claimedBy", claimedBy,
                "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * @Traceability: US-002 - CA-23
     * claim-next con SKIP LOCKED. Reclama la siguiente tarea más urgente de forma atómica.
     */
    @Transactional
    public AgileTask claimNextTask(String claimedBy) {
        AgileTask task = taskRepository.findNextAvailableTaskForUpdate()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay tareas disponibles en el pool"));
        
        task.setStatus("CLAIMED");
        if (task.getAssigneeIds() == null) {
            task.setAssigneeIds(new java.util.HashSet<>());
        }
        if (!task.getAssigneeIds().contains(claimedBy)) {
            task.getAssigneeIds().add(claimedBy);
        }
        taskRepository.save(task);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", com.ibpms.poc.domain.model.agile.WebSocketEventType.TASK_CLAIMED.name(),
                "taskId", task.getId(),
                "claimedBy", claimedBy,
                "timestamp", java.time.Instant.now()
        ));

        // CA-26: Refresco Automático de Cola STOMP
        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", com.ibpms.poc.domain.model.agile.WebSocketEventType.TASK_POOL_REFRESH.name(),
                "timestamp", java.time.Instant.now()
        ));
        
        return task;
    }

    /**
     * @Traceability: US-002 - CA-21
     * Rollback Optimistic UI. Libera la tarea SOLO si el asignado actual
     * concuerda con el solicitante (el fallback timeout).
     */
    @Transactional
    public void rollbackClaim(UUID taskId, String rollbackBy) {
        AgileTask task = getTaskForUpdate(taskId);
        
        if (task.getAssigneeIds() == null || !task.getAssigneeIds().contains(rollbackBy)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea ya fue reclamada por otro operador o no te pertenece");
        }
        
        task.setStatus("AVAILABLE");
        task.getAssigneeIds().remove(rollbackBy);
        taskRepository.save(task);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", com.ibpms.poc.domain.model.agile.WebSocketEventType.TASK_UNCLAIMED.name(),
                "taskId", taskId,
                "unclaimedBy", rollbackBy,
                "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * @Traceability: US-002 - CA-4
     * Libera una tarea y emite un evento WebSocket a la UI.
     */
    @Transactional
    public void unclaimTask(UUID taskId, String unclaimedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        if (task.getAssigneeIds() == null || !task.getAssigneeIds().contains(unclaimedBy)) {
            throw new com.ibpms.poc.domain.exception.TaskOwnershipViolationException(unclaimedBy);
        }
        task.setStatus("AVAILABLE");
        task.getAssigneeIds().remove(unclaimedBy);
        taskRepository.save(task);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", com.ibpms.poc.domain.model.agile.WebSocketEventType.TASK_UNCLAIMED.name(),
                "taskId", taskId,
                "unclaimedBy", unclaimedBy,
                "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * @Traceability: US-002 - CA-8
     * Force Unclaim de un Supervisor
     * Libera la tarea anulando las validaciones de ownership y notifica.
     */
    @Transactional
    public void forceUnclaimTask(UUID taskId) {
        AgileTask task = getTaskForUpdate(taskId);
        task.setStatus("AVAILABLE");
        if (task.getAssigneeIds() != null) {
            task.getAssigneeIds().clear();
        }
        taskRepository.save(task);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", "TASK_FORCE_UNCLAIMED",
                "taskId", taskId,
                "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * @Traceability: US-002 - CA-2
     * bulk-claim
     */
    @Transactional
    public java.util.Map<String, Object> bulkClaim(List<String> taskIds, String assignee) {
        List<String> claimed = new java.util.ArrayList<>();
        List<java.util.Map<String, String>> conflicts = new java.util.ArrayList<>();

        for (String idStr : taskIds) {
            UUID taskId = UUID.fromString(idStr);
            try {
                AgileTask task = getTaskForUpdate(taskId);
                if (!"OPEN".equals(task.getStatus()) && !"AVAILABLE".equals(task.getStatus())) {
                    conflicts.add(java.util.Map.of("taskId", idStr, "reason", "Task is not AVAILABLE"));
                    continue;
                }
                task.setStatus("CLAIMED");
                if (task.getAssigneeIds() == null) {
                    task.setAssigneeIds(new java.util.HashSet<>());
                }
                task.getAssigneeIds().add(assignee);
                taskRepository.save(task);

                claimAuditService.audit(taskId, assignee, "BULK_CLAIMED", null, null, null);
                claimed.add(idStr);

            } catch (Exception e) {
                conflicts.add(java.util.Map.of("taskId", idStr, "reason", "Concurrency conflict or not found"));
            }
        }

        if (!claimed.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                    "event", "BULK_REMOVE",
                    "taskIds", claimed,
                    "claimedBy", assignee
            ));
        }

        return java.util.Map.of("claimed", claimed, "conflicts", conflicts);
    }

    /**
     * GAP-005: releaseTask
     */
    @Transactional
    public void releaseTask(UUID taskId, String assignee, String message) {
        AgileTask task = getTaskForUpdate(taskId);
        if (task.getAssigneeIds() == null || !task.getAssigneeIds().contains(assignee)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot release unowned task");
        }
        task.getAssigneeIds().remove(assignee);
        task.setStatus("AVAILABLE");
        taskRepository.save(task);

        claimAuditService.audit(taskId, assignee, "RELEASED", null, null, message);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", "ADD",
                "taskId", taskId
        ));
    }

    /**
     * GAP-006: forceUnclaimWithValidation
     */
    @Transactional
    public java.util.Map<String, String> forceUnclaimWithValidation(UUID taskId, String supervisorId, String supervisorTeamId) {
        AgileTask task = getTaskForUpdate(taskId);
        
        if (task.getTeamId() != null && !task.getTeamId().equals(supervisorTeamId)) {
            claimAuditService.audit(taskId, supervisorId, "DENIED", "Team mismatch", null, null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para gestionar tareas de este equipo");
        }

        String previousAssignee = task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty() 
            ? task.getAssigneeIds().iterator().next() : null;

        task.setStatus("AVAILABLE");
        if (task.getAssigneeIds() != null) {
            task.getAssigneeIds().clear();
        }
        taskRepository.save(task);

        claimAuditService.audit(taskId, supervisorId, "FORCE_UNCLAIMED", null, previousAssignee, null);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", "TASK_FORCE_UNCLAIMED",
                "taskId", taskId,
                "timestamp", java.time.Instant.now()
        ));

        return java.util.Map.of(
            "taskId", taskId.toString(),
            "previousAssignee", previousAssignee != null ? previousAssignee : "none",
            "forcedBy", supervisorId,
            "timestamp", java.time.Instant.now().toString()
        );
    }

    /**
     * GAP-008: extendTimeout
     */
    @Transactional
    public void extendTimeout(UUID taskId, String assignee) {
        AgileTask task = getTaskForUpdate(taskId);
        if (task.getAssigneeIds() == null || !task.getAssigneeIds().contains(assignee)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el asignado de esta tarea");
        }
        if (task.getTimeoutExtensions() != null && task.getTimeoutExtensions() >= 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum extensions reached");
        }
        task.setTimeoutExtensions((task.getTimeoutExtensions() == null ? 0 : task.getTimeoutExtensions()) + 1);
        task.setLastActivityAt(java.time.ZonedDateTime.now());
        taskRepository.save(task);

        claimAuditService.audit(taskId, assignee, "TIMEOUT_EXTENDED", null, null, null);
    }
}
