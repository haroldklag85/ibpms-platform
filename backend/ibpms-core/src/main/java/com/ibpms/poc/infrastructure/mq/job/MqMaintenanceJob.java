package com.ibpms.poc.infrastructure.mq.job;

import com.ibpms.poc.infrastructure.jpa.repository.DlqArchiveRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MqMaintenanceJob {

    private static final Logger log = LoggerFactory.getLogger(MqMaintenanceJob.class);
    
    private final ProcessedMessageRepository processedMessageRepository;
    private final DlqArchiveRepository dlqArchiveRepository;

    public MqMaintenanceJob(ProcessedMessageRepository processedMessageRepository, DlqArchiveRepository dlqArchiveRepository) {
        this.processedMessageRepository = processedMessageRepository;
        this.dlqArchiveRepository = dlqArchiveRepository;
    }

    /**
     * Purga mensajes procesados con más de 72 horas para liberar espacio en la tabla de Idempotencia.
     */
    @Scheduled(fixedRate = 86400000) // 24 horas
    public void purgeIdempotencyKeys() {
        log.info("Iniciando purga de llaves de idempotencia > 72 horas");
        processedMessageRepository.deleteOlderThan(LocalDateTime.now().minusHours(72));
    }

    /**
     * Purga mensajes archivados de DLQ mayores a 180 días (Retención legal).
     */
    @Scheduled(fixedRate = 86400000) // 24 horas
    public void purgeDlqArchives() {
        log.info("Iniciando purga de archivo de DLQ > 180 días");
        dlqArchiveRepository.deleteOlderThan(LocalDateTime.now().minusDays(180));
    }
}
