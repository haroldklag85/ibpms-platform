package com.ibpms.poc.infrastructure.camunda;

import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CA-54: Redacción PII en Auditoría History (Seguridad Shift-Left)
 * Intercepta y enmascara variables sensibles antes de que toquen el Log Histórico de BD.
 */
@Component
public class PiiRedactionHistoryEventHandler implements HistoryEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PiiRedactionHistoryEventHandler.class);

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
        if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
            HistoricVariableUpdateEventEntity varEvent = (HistoricVariableUpdateEventEntity) historyEvent;
            
            String varName = varEvent.getVariableName() != null ? varEvent.getVariableName().toLowerCase() : "";
            // Heurística de detección de PII (Personally Identifiable Information)
            if (varName.contains("email") || varName.contains("password") || varName.contains("ssn") || varName.contains("tarjeta")) {
                log.warn("Interceptada variable sensible PII en motor histórico: {}. Redactando...", varEvent.getVariableName());
                varEvent.setTextValue("[REDACTED_PII]");
                varEvent.setDoubleValue(null);
                varEvent.setLongValue(null);
            }
        }
        // En una implementación real, aquí se delega al handler por defecto de Camunda (DbHistoryEventHandler)
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
        for (HistoryEvent event : historyEvents) {
            handleEvent(event);
        }
    }
}
