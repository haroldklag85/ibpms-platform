package com.ibpms.poc.application.service.intake;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Aplicación para gestionar payloads huérfanos/malformados.
 * 
 * @Traceability(US = "US-004", CA = {"CA-12"})
 */
@Service
@Transactional
@Traceability(US = "US-004", CA = {"CA-12"})
public class OrphanPayloadService {

    private final OrphanPayloadRepository orphanPayloadRepository;

    public OrphanPayloadService(OrphanPayloadRepository orphanPayloadRepository) {
        this.orphanPayloadRepository = orphanPayloadRepository;
    }

    /**
     * Guarda un payload huérfano.
     * @param orphanPayload Entidad de dominio.
     */
    // @Traceability: US-004 - CA-12 (ADR-001 Refactor)
    public void save(OrphanPayload orphanPayload) {
        orphanPayloadRepository.save(orphanPayload);
    }
}
