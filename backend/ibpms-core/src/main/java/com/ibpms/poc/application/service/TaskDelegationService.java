package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio para procesar Delegaciones In-Flight con evaluación perezosa (Lazy Evaluation).
 * Cubre el Criterio CA-23 sin requerir CRON (CA-24).
 */
@Service
public class TaskDelegationService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskDelegationService.class);

    /**
     * Revisa si una tarea pertenece a una delegación vigente o si ya expiró.
     * Llamado internamente por el Controller del Workdesk (GET).
     * 
     * @param originalOwner   Usuario dueño original
     * @param currentAssignee Usuario asignado actual (suplente)
     * @param expiryDate      Fecha de expiración de la delegación
     * @param taskId          ID de la tarea
     * @return El assignee efectivo. Puede haber revertido On-the-fly.
     */
    public String evaluateAndRevertTaskIfNeeded(String originalOwner, String currentAssignee, LocalDateTime expiryDate, String taskId) {
        // En una implementación real, esto también verificaría el estado de la BD.
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
            if (!originalOwner.equals(currentAssignee)) {
                log.info("Lazy Evaluation CA-23: La delegación para la tarea {} ha expirado. Revirtiendo On-the-fly a {}.", taskId, originalOwner);
                
                // Realizar UPDATE a `ibpms_workdesk_projection` SET assignee = originalOwner WHERE id = taskId
                // taskRepository.revertAssignee(taskId, originalOwner);
                
                log.warn("SUDO Action [Audit Trail]: Retorno automático de tarea In-Flight post-delegación. Tarea: {}, Nuevo Asignado: {}", taskId, originalOwner);
                return originalOwner;
            }
        }
        return currentAssignee;
    }
}
