package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import com.ibpms.poc.infrastructure.jpa.repository.InboundWebhookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InboundWebhookService {

    private final InboundWebhookRepository inboundWebhookRepository;

    public InboundWebhookService(InboundWebhookRepository inboundWebhookRepository) {
        this.inboundWebhookRepository = inboundWebhookRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public Optional<InboundWebhookEntity> findByIdAndIsActiveTrue(UUID id) {
        return inboundWebhookRepository.findByIdAndIsActiveTrue(id);
    }

    // @Traceability: Retro-Remediación ADR-001
    public java.util.List<InboundWebhookEntity> findAll() {
        return inboundWebhookRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public InboundWebhookEntity saveInboundWebhook(InboundWebhookEntity entity) {
        return inboundWebhookRepository.save(entity);
    }
}
