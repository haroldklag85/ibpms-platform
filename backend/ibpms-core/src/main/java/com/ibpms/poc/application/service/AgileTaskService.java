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

    public AgileTaskService(AgileTaskRepositoryJpa taskRepository, 
                            SlaChangeLogService slaChangeLogService, 
                            AuditLogService auditLogService,
                            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.slaChangeLogService = slaChangeLogService;
        this.auditLogService = auditLogService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public AgileTask createTask(UUID projectId, String title, String description, BigDecimal effort, String createdBy) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título obligatorio");
        }

        AgileTask task = AgileTask.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .title(title)
                .description(description)
                .effortEstimated(effort)
                .createdBy(createdBy)
                .build();

        return taskRepository.save(task);
    }

    public Page<AgileTask> listTasks(UUID projectId, boolean showCompleted, Pageable pageable) {
        return taskRepository.findByProjectIdAndStatusNot(projectId, "DELETED", pageable);
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
    public AgileTask updateTask(UUID taskId, String title, String description, BigDecimal effort, String status, java.time.ZonedDateTime slaDeadline, String updatedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (effort != null) task.setEffortEstimated(effort);
        if (status != null) task.setStatus(status);
        
        if (slaDeadline != null && !slaDeadline.equals(task.getSlaDeadline())) {
            slaChangeLogService.logSlaModification(taskId, task.getSlaDeadline(), slaDeadline, updatedBy);
            task.setSlaDeadline(slaDeadline);
        }
        
        return taskRepository.save(task);
    }

    /**
     * CA-4: Eliminar con auditoría forense (Soft Delete)
     * Utiliza save en vez de Query manual para forzar el Audit Log de Javers automáticamente.
     */
    @Transactional
    public void deleteTask(UUID taskId, String deletedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        task.setStatus("DELETED");
        taskRepository.save(task);
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
        for (UUID taskId : taskIds) {
            AgileTask task = getTaskForUpdate(taskId);
            task.getAssigneeIds().add(userId);
            taskRepository.save(task);
        }
    }

    /**
     * US-002: Reclama una tarea y emite un evento WebSocket a la UI.
     */
    @Transactional
    public void claimTask(UUID taskId, String claimedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        if (!"OPEN".equals(task.getStatus()) && !"AVAILABLE".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea no está disponible para reclamación");
        }
        task.setStatus("CLAIMED");
        if (task.getAssigneeIds() == null) {
            task.setAssigneeIds(new java.util.ArrayList<>());
        }
        if (!task.getAssigneeIds().contains(claimedBy)) {
            task.getAssigneeIds().add(claimedBy);
        }
        taskRepository.save(task);

        // US-002 CA-13: Emisión STOMP WebSocket
        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", "TASK_CLAIMED",
                "taskId", taskId,
                "claimedBy", claimedBy,
                "timestamp", java.time.Instant.now()
        ));
    }

    /**
     * US-002: Libera una tarea y emite un evento WebSocket a la UI.
     */
    @Transactional
    public void unclaimTask(UUID taskId, String unclaimedBy) {
        AgileTask task = getTaskForUpdate(taskId);
        if (!"CLAIMED".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea no está en estado CLAIMED");
        }
        task.setStatus("AVAILABLE");
        if (task.getAssigneeIds() != null) {
            task.getAssigneeIds().remove(unclaimedBy);
        }
        taskRepository.save(task);

        messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                "event", "TASK_UNCLAIMED",
                "taskId", taskId,
                "unclaimedBy", unclaimedBy,
                "timestamp", java.time.Instant.now()
        ));
    }
}
