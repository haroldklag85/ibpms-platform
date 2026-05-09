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

    // @Traceability(US = "US-001", CA = {"CA-06"})
    // TODO: Brecha CA-06, CA-14 y CA-27. Este publicador emite a un canal global ("/topic/workdesk/ghost-deletes") 
    // sin aislar por Tenant, exponiendo eventos de asignación a empresas cruzadas. Además, incumple el 
    // vocabulario atómico del CA-27 (debería emitir { action: 'REMOVE' }).
    @Override
    public void notify(DelegateTask delegateTask) {
        // Ignora eventos que no son de asignación
        if (!TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) return;
        
        Map<String, Object> payload = Map.of(
            "taskId", delegateTask.getId(),
            "assignee", delegateTask.getAssignee(),
            "status", "CLAIMED"
        );
        // Publicar evento STOMP al Frontend
        messagingTemplate.convertAndSend("/topic/workdesk/ghost-deletes", payload);
    }
}
