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
