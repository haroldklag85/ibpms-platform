package com.ibpms.poc.application.service.bpm;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Date;
import java.util.List;
import com.ibpms.poc.infrastructure.bpm.calendar.CustomBusinessCalendar;

/**
 * Servicio de Aplicación para Gestión de Service Level Agreements (SLA).
 * Gestiona feriados, horario de oficina y lógica de SLAs.
 * 
 * @Traceability(US = "US-010", CA = {"CA-02"})
 */
@Service
@Transactional
@Traceability(US = "US-010", CA = {"CA-02"})
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);
    private final TaskService camundaTaskService;
    private final CustomBusinessCalendar customBusinessCalendar;
    private final com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository holidayRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.bpm.BusinessHoursRepository businessHoursRepository;

    public SlaService(TaskService camundaTaskService, CustomBusinessCalendar customBusinessCalendar,
                      com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository holidayRepository,
                      com.ibpms.poc.infrastructure.jpa.repository.bpm.BusinessHoursRepository businessHoursRepository) {
        this.camundaTaskService = camundaTaskService;
        this.customBusinessCalendar = customBusinessCalendar;
        this.holidayRepository = holidayRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    /**
     * CA-3: Endpoint Anti-Deadlocks Retroactivos.
     * Itera las tareas asíncronamente en lotes y recalcula las fechas pasándoles un dummy ISO para invocar el CustomCalendar.
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
                if (task.getDueDate() != null && task.getCreateTime() != null) {
                    long durationMilli = task.getDueDate().getTime() - task.getCreateTime().getTime();
                    long hours = durationMilli / (1000 * 60 * 60);
                    if (hours <= 0) hours = 4; // Fallback
                    
                    String isoDuration = "PT" + hours + "H";
                    Date newCalculatedDate = customBusinessCalendar.resolveDuedate(isoDuration, task);
                    camundaTaskService.setDueDate(task.getId(), newCalculatedDate);
                    log.debug("Actualizado SLA para la tarea {}", task.getId());
                }
            }

            firstResult += pageSize;
        }

        log.info("[SLA_REC] Recálculo completado exitosamente.");
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity> getHolidays() {
        return holidayRepository.findAll();
    }

    /**
     * Agrega un feriado.
     */
    // @Traceability: US-010 - CA-02 (ADR-001 Refactor)
    public com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity addHoliday(com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity holiday) {
        return holidayRepository.save(holiday);
    }

    /**
     * Elimina un feriado por ID.
     */
    // @Traceability: US-010 - CA-02 (ADR-001 Refactor)
    public void deleteHoliday(java.util.UUID id) {
        holidayRepository.deleteById(id);
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity> getBusinessHours() {
        return businessHoursRepository.findAll();
    }

    /**
     * Guarda o actualiza la configuración de horas laborables.
     */
    // @Traceability: US-010 - CA-02 (ADR-001 Refactor)
    public com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity saveBusinessHours(com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity businessHours) {
        return businessHoursRepository.save(businessHours);
    }
}
