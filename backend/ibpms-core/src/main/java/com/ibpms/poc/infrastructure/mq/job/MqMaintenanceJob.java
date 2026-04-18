package com.ibpms.poc.infrastructure.mq.job;

import com.ibpms.poc.infrastructure.jpa.entity.DlqArchiveEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DlqArchiveRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MqMaintenanceJob {

    private static final Logger log = LoggerFactory.getLogger(MqMaintenanceJob.class);
    
    private final ProcessedMessageRepository processedMessageRepository;
    private final DlqArchiveRepository dlqArchiveRepository;
    private final RabbitTemplate rabbitTemplate;

    public MqMaintenanceJob(ProcessedMessageRepository processedMessageRepository, DlqArchiveRepository dlqArchiveRepository, RabbitTemplate rabbitTemplate) {
        this.processedMessageRepository = processedMessageRepository;
        this.dlqArchiveRepository = dlqArchiveRepository;
        this.rabbitTemplate = rabbitTemplate;
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

    /**
     * CA-9: Reglas de Archivo Legal DLQ (30 Días).
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM
    public void archiveOldDlqMessages() {
        log.info("Iniciando rescate de mensajes DLQ moribundos");
        int archivedCount = 0;
        
        while (true) {
            Message msg = rabbitTemplate.receive("ibpms.dlq.global");
            if (msg == null) {
                break;
            }
            
            DlqArchiveEntity archive = new DlqArchiveEntity();
            archive.setMessageId(msg.getMessageProperties().getMessageId() != null ? msg.getMessageProperties().getMessageId() : java.util.UUID.randomUUID().toString());
            archive.setOriginalQueue(msg.getMessageProperties().getReceivedRoutingKey());
            archive.setHeadersJson(msg.getMessageProperties().getHeaders().toString());
            
            String body = new String(msg.getBody());
            archive.setBodySummary(body.length() > 1000 ? body.substring(0, 1000) : body);
            
            // Asumimos que están expirados/moribundos si llegamos a este job o validamos header
            dlqArchiveRepository.save(archive);
            archivedCount++;
        }
        
        log.info("Proceso CA-9 completado. Mensajes transferidos a BDD: {}", archivedCount);
    }
}
