package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.persistence.AgileProjectRepositoryJpa;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class AgileProjectClosureService {

    private final AgileProjectRepositoryJpa projectRepository;
    private final AgileTaskRepositoryJpa taskRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AgileProjectClosureService(AgileProjectRepositoryJpa projectRepository,
                                      AgileTaskRepositoryJpa taskRepository,
                                      SimpMessagingTemplate messagingTemplate) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void closeProjectInCascade(UUID projectId, String closedBy) {
        log.info("Iniciando cierre en cascada del proyecto: {} por el usuario: {}", projectId, closedBy);

        // 1. Cerrar a nivel de proyecto
        projectRepository.updateStatus(projectId, "CLOSED");

        // 2. Operación Batch Nativa (Update Masivo de Tasks evitando N+1)
        int updated = taskRepository.bulkCancelTasks(projectId);
        log.info("Tareas canceladas masivamente: {}", updated);

        // 3. Emitir Websocket STOMP Alert (Destino de P10 Hub Ágil)
        String topic = "/topic/agile/projects/" + projectId + "/closure";
        String message = String.format("{\"event\": \"PROJECT_CLOSED\", \"by\": \"%s\", \"cancelledTasks\": %d}", closedBy, updated);
        
        try {
            messagingTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            // No bloqueamos la transacción si el bróker WS está caído.
            log.error("Fallo enviando evento Websocket de Cierre", e);
        }
    }
}
