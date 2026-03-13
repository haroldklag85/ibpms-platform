package com.ibpms.poc.infrastructure.event;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Event Listener Acoplado a JPA para replicar Kanban hacia la tabla CQRS (Workdesk).
 */
@Component
public class KanbanTaskSyncListener {

    private static final Logger log = LoggerFactory.getLogger(KanbanTaskSyncListener.class);

    // Se usa @Lazy para evitar dependencias circulares durante la inicializacion de Hibernate
    private WorkdeskProjectionRepository projectionRepository;
    
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public void setProjectionRepository(@Lazy WorkdeskProjectionRepository projectionRepository) {
        this.projectionRepository = projectionRepository;
    }

    @Autowired
    public void setMessagingTemplate(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostPersist
    @PostUpdate
    public void syncToProjection(KanbanTaskEntity task) {
        if (projectionRepository == null) {
            log.warn("WorkdeskProjectionRepository no inyectado, saltando sync de Kanban.");
            return;
        }

        try {
            WorkdeskProjectionEntity projection = projectionRepository.findById("KANBAN-" + task.getId())
                    .orElse(new WorkdeskProjectionEntity());

            projection.setId("KANBAN-" + task.getId());
            projection.setSourceSystem("KANBAN");
            projection.setOriginalTaskId(task.getId().toString());
            projection.setTitle(task.getTitle());
            projection.setAssignee(task.getAssignee());
            projection.setCandidateGroup(null); // Kanban simple no maneja grupos aquí
            projection.setSlaExpirationDate(task.getSlaDueDate());
            projection.setStatus(task.getStatus());
            
            projectionRepository.save(projection);
            log.debug("Kanban CQRS Sync exitoso para tarea {}", task.getId());
            
            // CA-6: Broadcastear TASK_CLAIMED para Ghost Deletion en Front
            if (task.getAssignee() != null && messagingTemplate != null) {
                String payload = "{\"event\": \"TASK_CLAIMED\", \"taskId\": \"KANBAN-" + task.getId() + "\"}";
                messagingTemplate.convertAndSend("/topic/workdesk.updates", payload);
            }
            
        } catch (Exception e) {
            log.error("Error sincronizando KanbanTask {} hacia Workdesk CQRS", task.getId(), e);
        }
    }
}
