package com.ibpms.poc.infrastructure.event;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Global TaskListener para interceptar Tareas de Usuario de Camunda hacia la tabla CQRS.
 * Se asume registro manual en el motor o global parse listener.
 */
@Component
public class CamundaTaskSyncListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(CamundaTaskSyncListener.class);
    
    private final WorkdeskProjectionRepository projectionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public CamundaTaskSyncListener(WorkdeskProjectionRepository projectionRepository, SimpMessagingTemplate messagingTemplate) {
        this.projectionRepository = projectionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String eventName = delegateTask.getEventName();
            String taskId = delegateTask.getId();

            if (EVENTNAME_DELETE.equals(eventName)) {
                projectionRepository.deleteById("BPMN-" + taskId);
                log.debug("BPMN CQRS Sync (Delete) exitoso para tarea {}", taskId);
                return;
            }

            WorkdeskProjectionEntity projection = projectionRepository.findById("BPMN-" + taskId)
                    .orElse(new WorkdeskProjectionEntity());

            projection.setId("BPMN-" + taskId);
            projection.setSourceSystem("BPMN");
            projection.setOriginalTaskId(taskId);
            projection.setTitle(delegateTask.getName() != null ? delegateTask.getName() : delegateTask.getTaskDefinitionKey());
            projection.setAssignee(delegateTask.getAssignee());
            projection.setCandidateGroup(delegateTask.getCandidates() != null && !delegateTask.getCandidates().isEmpty() ? 
                delegateTask.getCandidates().iterator().next().getGroupId() : null);
            
            Date dueDate = delegateTask.getDueDate();
            if (dueDate != null) {
                projection.setSlaExpirationDate(LocalDateTime.ofInstant(dueDate.toInstant(), ZoneId.systemDefault()));
            }

            // Mapeamos el ciclo de vida de Camunda al status de vista
            if (EVENTNAME_COMPLETE.equals(eventName)) {
                projection.setStatus("COMPLETED");
            } else {
                projection.setStatus(delegateTask.getAssignee() != null ? "IN_PROGRESS" : "PENDING");
            }

            // Opcional: Extraer variables del contexto para payload
            // String localVars = mapper.writeValueAsString(delegateTask.getVariablesLocal());
            
            projectionRepository.save(projection);
            log.debug("BPMN CQRS Sync ({}) exitoso para tarea {}", eventName, taskId);
            
            if ("assignment".equals(eventName) && delegateTask.getAssignee() != null) {
                String payload = "{\"event\": \"TASK_CLAIMED\", \"taskId\": \"BPMN-" + taskId + "\"}";
                messagingTemplate.convertAndSend("/topic/workdesk.updates", payload);
            }

        } catch (Exception e) {
            log.error("Error sincronizando CamundaTask {} hacia Workdesk CQRS", delegateTask.getId(), e);
        }
    }
}
