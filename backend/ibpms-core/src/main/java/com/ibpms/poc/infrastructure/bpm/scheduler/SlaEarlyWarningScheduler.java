package com.ibpms.poc.infrastructure.bpm.scheduler;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SlaEarlyWarningScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaEarlyWarningScheduler.class);
    
    private final TaskService taskService;
    private final ApplicationEventPublisher eventPublisher;

    public SlaEarlyWarningScheduler(TaskService taskService, ApplicationEventPublisher eventPublisher) {
        this.taskService = taskService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scanAndFlagAtRiskTasks() {
        log.info("[SLA_SCHEDULER] Iniciando escaneo de tareas activas para evaluar riesgos SLA al 80%");

        if (taskService == null) {
            return;
        }
        org.camunda.bpm.engine.task.TaskQuery query = taskService.createTaskQuery();
        if (query == null) {
            return;
        }
        List<Task> activeTasks = query.active().list();

        long now = System.currentTimeMillis();

        for (Task task : activeTasks) {
            if (task.getDueDate() != null && task.getCreateTime() != null) {
                Boolean isRiskFlagged = (Boolean) taskService.getVariableLocal(task.getId(), "isSlaAtRisk");
                
                if (Boolean.TRUE.equals(isRiskFlagged)) {
                    continue; // Ya flaggeada, saltar
                }

                long createdTimestamp = task.getCreateTime().getTime();
                long dueTimestamp = task.getDueDate().getTime();

                long elapsed = now - createdTimestamp;
                long total = dueTimestamp - createdTimestamp;

                if (total > 0) {
                    double percentage = (double) elapsed / total;

                    if (percentage >= 0.80 && percentage < 1.0) {
                        log.warn("[SLA_SCHEDULER] Tarea {} (PI: {}) alcanzó {}% de su SLA. Marcando en riesgo.", 
                            task.getId(), task.getProcessInstanceId(), String.format("%.2f", percentage * 100));
                        
                        taskService.setVariableLocal(task.getId(), "isSlaAtRisk", true);
                        eventPublisher.publishEvent(new SlaAtRiskEvent(this, task.getId(), task.getProcessInstanceId()));
                    }
                }
            }
        }
        log.info("[SLA_SCHEDULER] Escaneo de riesgos finalizado.");
    }
}
