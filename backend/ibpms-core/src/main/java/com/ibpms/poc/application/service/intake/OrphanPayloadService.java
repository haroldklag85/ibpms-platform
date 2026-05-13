package com.ibpms.poc.application.service.intake;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrphanPayloadService {

    private final OrphanPayloadRepository orphanPayloadRepository;

    public OrphanPayloadService(OrphanPayloadRepository orphanPayloadRepository) {
        this.orphanPayloadRepository = orphanPayloadRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public void save(OrphanPayload payload) {
        orphanPayloadRepository.save(payload);
    }
}
