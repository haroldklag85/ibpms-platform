package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.port.TriageTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class TriagePurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(TriagePurgeScheduler.class);
    private final TriageTaskRepository triageTaskRepository;

    public TriagePurgeScheduler(TriageTaskRepository triageTaskRepository) {
        this.triageTaskRepository = triageTaskRepository;
    }

    /**
     * CA-13: Ejecutado a las 2 AM diariamente.
     * Localiza reportes REJECTED con más de 30 días y efectúa una eliminación física.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void purgeRejectedTriages() {
        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(30);
        log.info("Iniciando purga programada (CA-13). Eliminando registros REJECTED de ibpms_triage_tasks anteriores a {}", cutoff);
        try {
            triageTaskRepository.deleteByStatusAndUpdatedAtBefore("REJECTED", cutoff);
            log.info("Purga programada TriageTask finalizada con éxito.");
        } catch (Exception e) {
            log.error("Fallo al ejecutar la purga TriageTask: {}", e.getMessage(), e);
        }
    }
}
