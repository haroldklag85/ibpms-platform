package com.ibpms.poc.infrastructure.event;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.Timestamp;

/**
 * Event Listener Acoplado a JPA para replicar Kanban hacia la tabla CQRS (Workdesk).
 * Remediación SRE: Se utiliza JdbcTemplate para evitar ConcurrentModificationException
 * al modificar el estado de la sesión de Hibernate durante la fase de Flush.
 */
@Component
public class KanbanTaskSyncListener {

    private static final Logger log = LoggerFactory.getLogger(KanbanTaskSyncListener.class);

    private JdbcTemplate jdbcTemplate;
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public void setJdbcTemplate(@Lazy JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setMessagingTemplate(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostPersist
    @PostUpdate
    public void syncToProjection(KanbanTaskEntity task) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate no inyectado, saltando sync de Kanban.");
            return;
        }

        try {
            String id = "KANBAN-" + task.getId();
            String sourceSystem = "KANBAN";
            String originalTaskId = task.getId().toString();
            String title = task.getTitle();
            String assignee = task.getAssignee();
            Timestamp slaExpirationDate = task.getSlaDueDate() != null ? Timestamp.valueOf(task.getSlaDueDate()) : null;
            String status = task.getStatus();

            String tenantId = "default";
            if (task.getBoard() != null && task.getBoard().getOwnerId() != null) {
                tenantId = task.getBoard().getOwnerId();
            }
            int impactLevel = 1;

            String sql = "INSERT INTO ibpms_workdesk_projection (" +
                    "id, source_system, original_task_id, title, assignee, candidate_group, " +
                    "sla_expiration_date, status, tenant_id, impact_level, progress_percent" +
                    ") VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, NULL) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "source_system = EXCLUDED.source_system, " +
                    "original_task_id = EXCLUDED.original_task_id, " +
                    "title = EXCLUDED.title, " +
                    "assignee = EXCLUDED.assignee, " +
                    "candidate_group = EXCLUDED.candidate_group, " +
                    "sla_expiration_date = EXCLUDED.sla_expiration_date, " +
                    "status = EXCLUDED.status, " +
                    "tenant_id = EXCLUDED.tenant_id, " +
                    "impact_level = EXCLUDED.impact_level, " +
                    "progress_percent = EXCLUDED.progress_percent";

            jdbcTemplate.update(sql, 
                    id, 
                    sourceSystem, 
                    originalTaskId, 
                    title, 
                    assignee, 
                    slaExpirationDate, 
                    status, 
                    tenantId, 
                    impactLevel
            );

            log.debug("Kanban CQRS Sync exitoso para tarea {}", task.getId());
            
            if (messagingTemplate != null) {
                String tenantIdWs = "default";
                
                com.ibpms.poc.application.dto.WsWorkdeskEventDTO wsEvent = new com.ibpms.poc.application.dto.WsWorkdeskEventDTO();
                wsEvent.setTaskId("KANBAN-" + task.getId());
                wsEvent.setTenantId(tenantIdWs);
                
                if (task.getAssignee() != null) {
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.REMOVE); // CA-06: Ghost deletion
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantIdWs, wsEvent);
                } else if ("PENDING".equals(task.getStatus())) {
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.ADD);
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantIdWs, wsEvent);
                } else {
                    wsEvent.setAction(com.ibpms.poc.application.dto.WsWorkdeskEventDTO.Action.UPDATE);
                    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantIdWs, wsEvent);
                }
            }
            
        } catch (Exception e) {
            log.error("Error sincronizando KanbanTask {} hacia Workdesk CQRS", task.getId(), e);
        }
    }
}
