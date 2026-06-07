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
            projection.setTitle("N/A");
            projection.setAssignee(null);
            projection.setCandidateGroup(null); // Kanban simple no maneja grupos aquí
            projection.setSlaExpirationDate(null);
            projection.setStatus(task.getStatus());

            // @Traceability(US = "US-001", CA = {"CA-23"})
            // TODO: Brecha de implementación CA-23. Falta calcular `progressPercent` en base a (Columna actual / Total Columnas Tablero).
            projection.setProgressPercent(null);
            
            projectionRepository.save(projection);
            log.debug("Kanban CQRS Sync exitoso para tarea {}", task.getId());
            
            if (messagingTemplate != null) {
                String tenantId = "default";
                
                com.ibpms.poc.application.dto.WsWorkdeskEventDTO wsEvent = new com.ibpms.poc.application.dto.WsWorkdeskEventDTO();
                wsEvent.setTaskId("KANBAN-" + task.getId());
                wsEvent.setTenantId(tenantId);
                
                if (false) { // Disabled assignee check since it's not in KanbanTaskEntity anymore
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.REMOVE); // CA-06: Ghost deletion
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
                } else if ("PENDING".equals(task.getStatus())) {
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.ADD);
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
                } else {
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.UPDATE);
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
                }
            }
            
        } catch (Exception e) {
            log.error("Error sincronizando KanbanTask {} hacia Workdesk CQRS", task.getId(), e);
        }
    }
}
