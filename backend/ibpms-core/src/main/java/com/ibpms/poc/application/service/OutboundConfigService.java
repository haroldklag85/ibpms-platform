package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.OutboundConfigEntity;
import com.ibpms.poc.infrastructure.jpa.repository.OutboundConfigRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Aplicación para gestionar Webhooks Salientes (Outbound).
 * Encapsula la lógica de persistencia para integraciones externas.
 * 
 * @Traceability(US = "US-023", CA = {"CA-02"})
 */
@Service
@Transactional
@Traceability(US = "US-023", CA = {"CA-02"})
public class OutboundConfigService {

    private final OutboundConfigRepository outboundConfigRepository;

    public OutboundConfigService(OutboundConfigRepository outboundConfigRepository) {
        this.outboundConfigRepository = outboundConfigRepository;
    }

    /**
     * Obtiene todas las configuraciones de salida registradas.
     * @return Lista de entidades de configuración.
     */
    // @Traceability: US-023 - CA-02 (ADR-001 Refactor)
    public List<OutboundConfigEntity> findAll() {
        return outboundConfigRepository.findAll();
    }

    /**
     * Obtiene la configuración de salida por su nombre.
     * @param targetName Nombre del destino.
     * @return Entidad de configuración de salida.
     */
    // @Traceability: US-023 - CA-02 (ADR-001 Refactor)
    public OutboundConfigEntity findByTargetName(String targetName) {
        return outboundConfigRepository.findByTargetName(targetName);
    }

    /**
     * Guarda o actualiza una configuración de salida.
     * @param entity Entidad a persistir.
     * @return Entidad guardada.
     */
    // @Traceability: US-023 - CA-02 (ADR-001 Refactor)
    public OutboundConfigEntity saveOutboundConfig(OutboundConfigEntity entity) {
        return outboundConfigRepository.save(entity);
    }
}
