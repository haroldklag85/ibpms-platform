package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.AiMetricsDTO;
import com.ibpms.poc.application.dto.ProcessHealthDTO;
import com.ibpms.poc.application.port.in.ObtenerMetricasUseCase;
import com.ibpms.poc.infrastructure.jpa.repository.AiAuditLogRepository;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ObtenerMetricasService implements ObtenerMetricasUseCase {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;
    private final AiAuditLogRepository aiAuditLogRepository;

    public ObtenerMetricasService(
            RuntimeService runtimeService,
            HistoryService historyService,
            TaskService taskService,
            AiAuditLogRepository aiAuditLogRepository) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.taskService = taskService;
        this.aiAuditLogRepository = aiAuditLogRepository;
    }

    @Override
    public ProcessHealthDTO getProcessHealth() {

        // 1. Casos Activos y Completados desde Camunda API
        long activeCases = runtimeService.createProcessInstanceQuery().active().count();
        long completedCases = historyService.createHistoricProcessInstanceQuery().completed().count();

        // 2. Tareas Activas y Tareas Atrasadas (Overdue)
        long activeTasks = taskService.createTaskQuery().active().count();
        long overdueTasks = taskService.createTaskQuery().taskDueBefore(new Date()).count();

        return new ProcessHealthDTO(activeCases, completedCases, activeTasks, overdueTasks);
    }

    @Override
    public AiMetricsDTO getAiMetrics() {

        long totalEvents = aiAuditLogRepository.count();
        long autoDmns = aiAuditLogRepository.countByEventType("DMN_GENERATION");
        long autoEmails = aiAuditLogRepository.countByEventType("EMAIL_NLU_ROUTING");
        Double avgSimilarity = aiAuditLogRepository.getAverageSimilarityScore();

        AiMetricsDTO metrics = new AiMetricsDTO();
        metrics.setTotalAiEvents(totalEvents);
        metrics.setGeneratedDmns(autoDmns);
        metrics.setAutoRoutedEmails(autoEmails);
        metrics.setAverageSimilarityScore(avgSimilarity != null ? avgSimilarity : 0.0);

        return metrics;
    }
}
