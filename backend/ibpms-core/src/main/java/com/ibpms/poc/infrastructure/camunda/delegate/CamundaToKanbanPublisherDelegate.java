package com.ibpms.poc.infrastructure.camunda.delegate;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.application.port.in.MoveKanbanTaskUseCase;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Delegate de Camunda invocado al finalizar el flujo de macro-aprobación.
 * Publica la decisión de vuelta al componente Ágil (Kanban) como requiere CA-07.
 */
@Component("camundaToKanbanPublisherDelegate")
@Traceability(US = "US-008", CA = {"CA-07"})
public class CamundaToKanbanPublisherDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CamundaToKanbanPublisherDelegate.class);
    private final MoveKanbanTaskUseCase moveKanbanTaskUseCase;

    public CamundaToKanbanPublisherDelegate(MoveKanbanTaskUseCase moveKanbanTaskUseCase) {
        this.moveKanbanTaskUseCase = moveKanbanTaskUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String kanbanTaskIdStr = (String) execution.getVariable("kanbanTaskId");
        Boolean isApproved = (Boolean) execution.getVariable("macroApprovalDecision");
        String systemUser = "System-Camunda";

        if (kanbanTaskIdStr != null) {
            UUID kanbanTaskId = UUID.fromString(kanbanTaskIdStr);
            String finalState = (isApproved != null && isApproved) ? "DONE" : "IN_PROGRESS";
            
            log.info("CA-07: Camunda emitiendo retorno al Kanban para Tarea {}. Decisión: {}. Estado Final: {}", 
                     kanbanTaskId, isApproved, finalState);
            
            try {
                // Invocamos la mutación hacia el componente ágil directamente a través del puerto de entrada
                moveKanbanTaskUseCase.moveTask(kanbanTaskId, finalState, null, systemUser);
                log.info("CA-07: Retorno de Camunda aplicado exitosamente en Kanban.");
            } catch (Exception e) {
                log.error("CA-07: Fallo al propagar decisión de Camunda a Kanban: {}", e.getMessage());
            }
        } else {
            log.warn("CA-07: Ejecución de Camunda finalizada pero sin vinculación a Kanban Task (kanbanTaskId ausente).");
        }
    }
}
