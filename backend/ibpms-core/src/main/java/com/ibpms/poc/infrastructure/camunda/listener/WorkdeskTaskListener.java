package com.ibpms.poc.infrastructure.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WorkdeskTaskListener implements TaskListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WorkdeskTaskListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // @Traceability(US = "US-001", CA = {"CA-06", "CA-14", "CA-27"})
    // REMEDIACIÓN: Emisión aislada por Tenant y usando vocabulario atómico (GHOST_CLAIM).
    @Override
    public void notify(DelegateTask delegateTask) {
        // Ignora eventos que no son de asignación
        if (!TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) return;
        
        String tenantId = delegateTask.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "default";
        }
        
        Map<String, Object> payload = Map.of(
            "action", "GHOST_CLAIM",
            "taskId", delegateTask.getId(),
            "assignee", delegateTask.getAssignee() != null ? delegateTask.getAssignee() : ""
        );
        // Publicar evento STOMP al Frontend en el topic segregado
        messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, payload);
    }
}
