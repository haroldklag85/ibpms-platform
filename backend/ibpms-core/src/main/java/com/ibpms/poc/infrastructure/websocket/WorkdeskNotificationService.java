package com.ibpms.poc.infrastructure.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WorkdeskNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WorkdeskNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Emite un evento STOMP indicando que una tarea ha sido reclamada.
     * El Frontend lo usará para realizar "Ghost Deletion" o "Optimistic Hide" de la grilla.
     */
    public void notifyTaskClaimed(String tenantId, String taskId, String claimedBy) {
        String destination = "/topic/workdesk/" + tenantId;
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TASK_CLAIMED");
        payload.put("taskId", taskId);
        payload.put("claimedBy", claimedBy);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Emite un evento STOMP indicando que una tarea ha sido liberada (Voluntariamente o por vencimiento SLA).
     */
    public void notifyTaskUnclaimed(String tenantId, String taskId) {
        String destination = "/topic/workdesk/" + tenantId;
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TASK_UNCLAIMED");
        payload.put("taskId", taskId);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * CA-8: Emite un evento STOMP indicando que un supervisor forzó la liberación de una tarea.
     */
    public void notifyTaskForceUnclaimed(String tenantId, String taskId) {
        String destination = "/topic/workdesk/" + tenantId;
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TASK_FORCE_UNCLAIMED");
        payload.put("taskId", taskId);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(destination, payload);
    }
}
