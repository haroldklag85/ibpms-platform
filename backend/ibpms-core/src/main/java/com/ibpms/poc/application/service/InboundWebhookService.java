package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import com.ibpms.poc.infrastructure.jpa.repository.InboundWebhookRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de Aplicación para gestionar Webhooks Entrantes (Inbound).
 * Encapsula la lógica de persistencia para el enrutamiento dinámico.
 * 
 * @Traceability(US = "US-023", CA = {"CA-01"})
 */
@Service
@Transactional
@Traceability(US = "US-023", CA = {"CA-01"})
public class InboundWebhookService {

    private final InboundWebhookRepository inboundWebhookRepository;

    public InboundWebhookService(InboundWebhookRepository inboundWebhookRepository) {
        this.inboundWebhookRepository = inboundWebhookRepository;
    }

    /**
     * Obtiene una configuración de webhook por su ID si está activa.
     * @param id ID del webhook a buscar.
     * @return Entidad InboundWebhookEntity si existe y está activa.
     */
    // @Traceability: US-023 - CA-01 (ADR-001 Refactor)
    public Optional<InboundWebhookEntity> findByIdAndIsActiveTrue(UUID id) {
        return inboundWebhookRepository.findByIdAndIsActiveTrue(id);
    }

    // @Traceability: US-023 - CA-01 (ADR-001 Refactor)
    public java.util.List<InboundWebhookEntity> findAll() {
        return inboundWebhookRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public InboundWebhookEntity saveInboundWebhook(InboundWebhookEntity entity) {
        return inboundWebhookRepository.save(entity);
    }
}
