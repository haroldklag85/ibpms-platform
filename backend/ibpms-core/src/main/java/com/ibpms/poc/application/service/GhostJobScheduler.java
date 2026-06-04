package com.ibpms.poc.application.service;

import com.ibpms.poc.application.config.ClaimProperties;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.enums.ClaimActionType;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CA-06/CA-15: Detección y auto-unclaim de tareas "ghost" (reclamadas pero sin actividad).
 *
 * <p>Cada 15 minutos, verifica las tareas CLAIMED agrupadas por tenant,
 * aplicando el timeout configurado para cada uno (con fallback al default global de 240 min).
 * Emite un pre-aviso al 75% del umbral y ejecuta auto-unclaim al alcanzarlo.</p>
 *
 * @see ClaimProperties para la configuración de tiempos por tenant.
 */
@Service
public class GhostJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(GhostJobScheduler.class);
    private static final String DEFAULT_TENANT = "default";

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

    /**
     * CA-15: Job periódico para detectar y procesar ghost jobs con timeout per-tenant.
     * Se ejecuta cada 15 minutos (900,000 ms).
     */
    @Scheduled(fixedRate = 900000)
    @Transactional
    public void detectAndProcessGhostTasks() {
        List<AgileTask> activeTasks = taskRepository.findByStatus("CLAIMED");

        ZonedDateTime now = ZonedDateTime.now();

        // CA-15: Agrupar por tenant para aplicar threshold diferenciado
        Map<String, List<AgileTask>> tasksByTenant = activeTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTenantId() != null ? t.getTenantId() : DEFAULT_TENANT));

        for (Map.Entry<String, List<AgileTask>> entry : tasksByTenant.entrySet()) {
            String tenantId = entry.getKey();
            int thresholdMins = claimProperties.getTimeoutForTenant(tenantId);
            int warningThresholdMins = (int) (thresholdMins * 0.75);

            for (AgileTask task : entry.getValue()) {
                ZonedDateTime lastActivity = task.getLastActivityAt();
                if (lastActivity == null) {
                    lastActivity = task.getCreatedAt();
                }

                long elapsedMinutes = Duration.between(lastActivity, now).toMinutes();

                if (elapsedMinutes >= thresholdMins) {
                    executeAutoUnclaim(task, thresholdMins);
                } else if (elapsedMinutes >= warningThresholdMins) {
                    emitGhostWarning(task, thresholdMins - elapsedMinutes);
                }
            }
        }
    }

    private void executeAutoUnclaim(AgileTask task, int thresholdMins) {
        String previousAssignee = task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()
                ? task.getAssigneeIds().iterator().next() : null;

        task.setStatus("AVAILABLE");
        if (task.getAssigneeIds() != null) {
            task.getAssigneeIds().clear();
        }
        task.setTimeoutExtensions(0); // CA-19: Reset extensiones al liberar
        taskRepository.save(task);

        claimAuditService.audit(task.getId(), "system",
                ClaimActionType.AUTO_UNCLAIMED.name(),
                "Ghost timeout reached (" + thresholdMins + " min)",
                previousAssignee, null);

        log.info("[AUTO_UNCLAIM] Tarea {} auto-unclaimed después de {} min de inactividad (tenant: {})",
                task.getId(), thresholdMins, task.getTenantId());

        // WebSocket REMOVE personal y ADD grupal
        if (previousAssignee != null) {
            messagingTemplate.convertAndSend("/topic/tasks/user/" + previousAssignee, Map.of(
                    "event", "REMOVE",
                    "taskId", task.getId()
            ));
        }
        messagingTemplate.convertAndSend("/topic/tasks", Map.of(
                "event", "ADD",
                "taskId", task.getId()
        ));
    }

    private void emitGhostWarning(AgileTask task, long remainingMinutes) {
        String assignee = task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()
                ? task.getAssigneeIds().iterator().next() : null;

        if (assignee != null) {
            messagingTemplate.convertAndSend("/topic/tasks/user/" + assignee, Map.of(
                    "event", "GHOST_WARNING",
                    "taskId", task.getId(),
                    "message", "Su tarea expirará en " + remainingMinutes + " minutos por inactividad"
            ));
            log.warn("[GHOST_WARNING] Tarea {} asignada a {} — {} min restantes",
                    task.getId(), assignee, remainingMinutes);
        }
    }
}
