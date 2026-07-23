package com.ibpms.poc.infrastructure.job;

import com.ibpms.poc.domain.port.FormEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
public class EventStoreArchivalScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStoreArchivalScheduler.class);

    private final FormEventRepository formEventRepository;

    public EventStoreArchivalScheduler(FormEventRepository formEventRepository) {
        this.formEventRepository = formEventRepository;
    }

    /**
     * CA-18: Política de Archivado Anual del Event Store.
     * Elimina eventos con más de 1 año (365 días) de antigüedad.
     * Se ejecuta todos los días a las 2 AM (cron: "0 0 2 * * ?").
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void archiveOldEvents() {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(365);
        log.info("Iniciando purga de FormEventStore para eventos anteriores a: {}", cutoffDate);
        
        try {
            formEventRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Purga de FormEventStore completada con éxito.");
        } catch (Exception e) {
            log.error("Error ejecutando la purga anual del Event Store", e);
        }
    }
}
