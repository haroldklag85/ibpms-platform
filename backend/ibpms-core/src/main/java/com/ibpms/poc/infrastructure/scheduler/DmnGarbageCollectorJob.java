package com.ibpms.poc.infrastructure.scheduler;

import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CA-03: Garbage Collector de XMLs Temporales.
 */
@Component
public class DmnGarbageCollectorJob {

    private static final Logger log = LoggerFactory.getLogger(DmnGarbageCollectorJob.class);
    private final DmnModelRepository dmnRepository;

    public DmnGarbageCollectorJob(DmnModelRepository dmnRepository) {
        this.dmnRepository = dmnRepository;
    }

    /**
     * Purga físicamente de PostgreSQL cualquier XML "DRAFT" que supere las 24 horas.
     * Se ejecuta todos los días a las 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeStaleDmnDrafts() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        List<DmnModelEntity> staleDrafts = dmnRepository.findByStatusAndUpdatedAtBefore("DRAFT", twentyFourHoursAgo);
        
        if (!staleDrafts.isEmpty()) {
            log.info("[SRE-GC] Detectados {} DMNs temporales obsoletos (>24h). Secuenciando purga física...", staleDrafts.size());
            dmnRepository.deleteAll(staleDrafts);
            log.info("[SRE-GC] Purga completada. Almacenamiento PostgreSQL liberado exitosamente.");
        } else {
            log.debug("[SRE-GC] No existen DMNs borradores listos para purgar.");
        }
    }
}
