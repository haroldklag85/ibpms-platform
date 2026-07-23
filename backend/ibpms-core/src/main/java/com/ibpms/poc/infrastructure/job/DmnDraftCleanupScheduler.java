// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.job;

import com.ibpms.poc.domain.model.DmnModel;
import com.ibpms.poc.domain.port.DmnModelRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CA-3 (US-007): Scheduler para purga de borradores DMN huérfanos generados por la IA.
 * Se ejecuta cada noche a las 3 AM y borra los DMNs en borrador con más de 30 días de antigüedad.
 */
@Component
public class DmnDraftCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DmnDraftCleanupScheduler.class);

    private final DmnModelRepositoryPort dmnRepository;
    
    public DmnDraftCleanupScheduler(DmnModelRepositoryPort dmnRepository) {
       this.dmnRepository = dmnRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Se ejecuta cada hora
    public void cleanupOldDrafts() {
        logger.info("[SRE] Iniciando depuración de borradores DMN antiguos (>72 horas)");
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(72);
            List<DmnModel> drafts = dmnRepository.findByStatusAndUpdatedAtBefore("DRAFT", cutoff);
            
            if (!drafts.isEmpty()) {
                for (DmnModel draft : drafts) {
                    dmnRepository.delete(draft);
                }
                logger.info("[SRE] Purga de DMN ejecutada exitosamente. Eliminados: {}", drafts.size());
            } else {
                logger.info("[SRE] No se encontraron borradores DMN expirados.");
            }
        } catch (Exception ex) {
            logger.error("[SRE] Error ejecutando la purga de DMN drafts", ex);
        }
    }
}
