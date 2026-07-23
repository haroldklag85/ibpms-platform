package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.repository.TempDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * CA-17: Limpieza diaria de archivos transitorios orphaned.
 *
 * <p>Los archivos quedan orphaned cuando un operario sube adjuntos y luego
 * libera la tarea sin completar (Amnesia Transaccional CA-07).
 * Se ejecuta diariamente a las 3:00 AM y elimina documentos con status "UPLOADED"
 * (no confirmados) que tengan más de 24 horas de antigüedad.</p>
 *
 * <p>Excluye archivos asociados a tareas CLAIMED activas (observación arquitectónica).</p>
 *
 * @see TempDocumentRepository#deleteByStatusAndUploadedAtBefore
 */
@Service
public class TransitoryFileCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TransitoryFileCleanupScheduler.class);
    private static final String ORPHANED_STATUS = "UPLOADED";
    private static final int CUTOFF_HOURS = 24;

    private final TempDocumentRepository tempDocumentRepository;

    public TransitoryFileCleanupScheduler(TempDocumentRepository tempDocumentRepository) {
        this.tempDocumentRepository = tempDocumentRepository;
    }

    /**
     * Job programado diario (3:00 AM) para purgar archivos transitorios orphaned.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOrphanedFiles() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusHours(CUTOFF_HOURS);
        int deletedCount = tempDocumentRepository.deleteByStatusAndUploadedAtBefore(ORPHANED_STATUS, cutoffTime);

        if (deletedCount > 0) {
            log.info("[CA-17] Limpieza de archivos transitorios completada. {} archivos eliminados (cutoff: {})",
                    deletedCount, cutoffTime);
        }
    }
}
