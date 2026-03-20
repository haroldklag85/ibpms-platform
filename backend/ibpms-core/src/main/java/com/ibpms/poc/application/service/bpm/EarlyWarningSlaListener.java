package com.ibpms.poc.application.service.bpm;

import com.ibpms.poc.application.event.SlaAtRiskEvent;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * CA-6 (Deuda Técnica US-043): Early Warning SLA
 * Este componente se enlazará a los Boundary Timers (No-interrumpibles) al 80% del tiempo de resolución.
 * Otorga visibilidad a la UI mediante variables locales e informa a otros módulos (EJ: Notificaciones).
 */
@Component("earlyWarningSlaListener")
public class EarlyWarningSlaListener implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(EarlyWarningSlaListener.class);
    private final ApplicationEventPublisher eventPublisher;

    public EarlyWarningSlaListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String procInstId = execution.getProcessInstanceId();
        String currentActivityId = execution.getCurrentActivityId();

        log.warn("[SLA WARNING] El proceso {} (Actividad: {}) está al 80% de su SLA (Quiebre inminente). Activando protocolo de rescate.", 
                 procInstId, currentActivityId);

        // 1. Visibilidad Transaccional: Inyección del booleano para el Frontend Vue.js
        // Se usa Variable Local para que pertenezca estricta e individualmente al Task/Execution en peligro.
        execution.setVariableLocal("isSlaAtRisk", true);

        // 2. Desacoplamiento de Correo (Observer en Memoria): Emisión del evento
        SlaAtRiskEvent riskEvent = new SlaAtRiskEvent(procInstId, execution.getId(), currentActivityId);
        eventPublisher.publishEvent(riskEvent);

        log.debug("[SLA WARNING] Evento SlaAtRiskEvent disparado localmente en RAM y Variable [isSlaAtRisk=true] materializada.");
    }
}
