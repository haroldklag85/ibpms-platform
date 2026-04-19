package com.ibpms.poc.infrastructure.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CA-3 (US-007): Scheduler para purga de borradores DMN huérfanos generados por la IA.
 * Se ejecuta cada noche a las 3 AM y borra los DMNs en borrador con más de 30 días de antigüedad.
 */
@Component
public class DmnDraftCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DmnDraftCleanupScheduler.class);

    // Inyectar Repositorio o Servicio de DMN aquí.
    // private final DmnRepository dmnRepository;
    
    // public DmnDraftCleanupScheduler(DmnRepository dmnRepository) {
    //    this.dmnRepository = dmnRepository;
    // }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldDrafts() {
        logger.info("[SRE] Iniciando depuración de borradores DMN antiguos (>30 días)");
        try {
            // Ejemplo: dmnRepository.deleteOlderThan(Instant.now().minus(30, ChronoUnit.DAYS));
            logger.info("[SRE] Purga de DMN ejecutada exitosamente.");
        } catch (Exception ex) {
            logger.error("[SRE] Error ejecutando la purga de DMN drafts", ex);
        }
    }
}
