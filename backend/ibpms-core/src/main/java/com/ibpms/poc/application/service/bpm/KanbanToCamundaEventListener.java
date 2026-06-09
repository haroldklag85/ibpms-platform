package com.ibpms.poc.application.service.bpm;

import com.ibpms.poc.domain.model.kanban.events.KanbanTaskStatusChangedEvent;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Escucha eventos de mutación de tareas Kanban para orquestar flujos BPMN (Camunda).
 * Cumple con el Salto Híbrido requerido en CA-07.
 */
@Component
@Traceability(US = "US-008", CA = {"CA-07"})
public class KanbanToCamundaEventListener {

    private static final Logger log = LoggerFactory.getLogger(KanbanToCamundaEventListener.class);
    private final RuntimeService camundaRuntimeService;

    public KanbanToCamundaEventListener(RuntimeService camundaRuntimeService) {
        this.camundaRuntimeService = camundaRuntimeService;
    }

    @Async
    @EventListener
    public void handleKanbanTaskStatusChangedEvent(KanbanTaskStatusChangedEvent event) {
        log.info("CA-07: Recibido evento asíncrono Kanban para Tarea {}. Estado: {}", event.getTaskId(), event.getNewState());

        if ("DONE".equals(event.getNewState()) || "QA_APPROVAL".equals(event.getNewState())) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("kanbanTaskId", event.getTaskId().toString());
            variables.put("originatingUser", event.getUserId());
            variables.put("macroApprovalRequired", true);

            try {
                // Instanciación aislada del Workflow estructurado
                camundaRuntimeService.startProcessInstanceByKey("macro_approval_process", event.getTaskId().toString(), variables);
                log.info("CA-07: Process Instantiation exitosa en Camunda para la tarea Kanban {}", event.getTaskId());
            } catch (Exception e) {
                log.error("CA-07: Fallo al instanciar flujo Camunda para la tarea Kanban {}. Detalle: {}", event.getTaskId(), e.getMessage());
            }
        }
    }
}
