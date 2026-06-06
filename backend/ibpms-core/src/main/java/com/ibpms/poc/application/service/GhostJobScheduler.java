package com.ibpms.poc.application.service;

import com.ibpms.poc.application.config.ClaimProperties;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class GhostJobScheduler {

    private final AgileTaskRepositoryJpa taskRepository;
    private final ClaimAuditService claimAuditService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClaimProperties claimProperties;

    public GhostJobScheduler(AgileTaskRepositoryJpa taskRepository,
                             ClaimAuditService claimAuditService,
                             SimpMessagingTemplate messagingTemplate,
                             ClaimProperties claimProperties) {
        this.taskRepository = taskRepository;
        this.claimAuditService = claimAuditService;
        this.messagingTemplate = messagingTemplate;
        this.claimProperties = claimProperties;
    }

    @Scheduled(fixedRate = 900000) // cada 15 minutos
    @Transactional
    public void detectAndProcessGhostTasks() {
        // Obtenemos todas las tareas "CLAIMED"
        List<AgileTask> activeTasks = taskRepository.findByStatus("CLAIMED");

        ZonedDateTime now = ZonedDateTime.now();
        int defaultTimeoutMins = claimProperties.getGhostTimeout(); // 240 mins

        for (AgileTask task : activeTasks) {
            // Asumimos tenant global al no haber tenantId en AgileTask
            int thresholdMins = defaultTimeoutMins; 
            
            ZonedDateTime lastActivity = task.getLastActivityAt();
            if (lastActivity == null) {
                lastActivity = task.getCreatedAt();
            }

            long elapsedMinutes = java.time.Duration.between(lastActivity, now).toMinutes();

            if (elapsedMinutes >= thresholdMins) {
                // Auto-unclaim
                String previousAssignee = task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty() 
                    ? task.getAssigneeIds().iterator().next() : null;

                task.setStatus("AVAILABLE");
                if (task.getAssigneeIds() != null) {
                    task.getAssigneeIds().clear();
                }
                taskRepository.save(task);

                claimAuditService.audit(task.getId(), "system", "AUTO_UNCLAIMED", "Ghost timeout reached", previousAssignee, null);

                // WebSocket REMOVE personal y ADD grupal
                messagingTemplate.convertAndSend("/topic/tasks/user/" + previousAssignee, java.util.Map.of(
                        "event", "REMOVE",
                        "taskId", task.getId()
                ));
                messagingTemplate.convertAndSend("/topic/tasks", java.util.Map.of(
                        "event", "ADD",
                        "taskId", task.getId()
                ));

            } else if (elapsedMinutes >= (thresholdMins * 0.75)) {
                // Pre-aviso al 75%
                String assignee = task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty() 
                    ? task.getAssigneeIds().iterator().next() : null;
                    
                if (assignee != null) {
                    messagingTemplate.convertAndSend("/topic/tasks/user/" + assignee, java.util.Map.of(
                            "event", "GHOST_WARNING",
                            "taskId", task.getId(),
                            "message", "Su tarea expirará pronto por inactividad"
                    ));
                }
            }
        }
    }
}
