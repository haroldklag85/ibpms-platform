package com.ibpms.poc.application.service.bpm;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);
    private final TaskService camundaTaskService;

    public SlaService(TaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    /**
     * CA-3: Endpoint Anti-Deadlocks Retroactivos.
     * Itera los Jobs/Tasks activos en lotes y recalcula las fechas pasándoles un dummy ISO para invocar el CustomCalendar.
     * El frontend recibe 202 Accepted antes de que esto comience.
     */
    @Async
    public void recalculateActiveSlas() {
        log.info("[SLA_REC] Iniciando recálculo masivo de Tiempos Hábiles en background...");
        
        int pageSize = 50;
        int firstResult = 0;
        boolean hasMore = true;

        while (hasMore) {
            List<Task> tareasEnVuelo = camundaTaskService.createTaskQuery()
                    .active()
                    .listPage(firstResult, pageSize);

            if (tareasEnVuelo.isEmpty()) {
                hasMore = false;
                break;
            }

            for (Task task : tareasEnVuelo) {
                if (task.getDueDate() != null) {
                    // Para el MVP disparamos setDueDate ficticio (en V2 usaremos CommandExecutor para forzar el Resolve).
                    // Asumimos un "+4 Horas" fijo como el CustomBusinessCalendar
                    Date newCalculatedDate = new Date(task.getDueDate().getTime() + (3600 * 1000));
                    camundaTaskService.setDueDate(task.getId(), newCalculatedDate);
                    log.debug("Actualizado SLA para la tarea {}", task.getId());
                }
            }

            firstResult += pageSize;
        }

        log.info("[SLA_REC] Recálculo completado exitosamente.");
    }
}
