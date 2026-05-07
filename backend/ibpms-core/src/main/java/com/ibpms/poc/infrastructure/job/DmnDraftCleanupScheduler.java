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

    private final com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository dmnRepository;
    
    public DmnDraftCleanupScheduler(com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository dmnRepository) {
       this.dmnRepository = dmnRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Se ejecuta cada hora
    public void cleanupOldDrafts() {
        logger.info("[SRE] Iniciando depuración de borradores DMN antiguos (>72 horas)");
        try {
            java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusHours(72);
            java.util.List<com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity> drafts = dmnRepository.findByStatusAndUpdatedAtBefore("DRAFT", cutoff);
            
            if (!drafts.isEmpty()) {
                dmnRepository.deleteAll(drafts);
                logger.info("[SRE] Purga de DMN ejecutada exitosamente. Eliminados: {}", drafts.size());
            } else {
                logger.info("[SRE] No se encontraron borradores DMN expirados.");
            }
        } catch (Exception ex) {
            logger.error("[SRE] Error ejecutando la purga de DMN drafts", ex);
        }
    }
}
