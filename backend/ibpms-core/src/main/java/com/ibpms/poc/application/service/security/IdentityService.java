package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.IdentityEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.IdentityRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Aplicación para Gestión de Identidades (Entra ID Sync).
 * 
 * @Traceability(US = "US-036", CA = {"CA-16"})
 */
@Service("ibpmsIdentityService")
@Transactional
@Traceability(US = "US-036", CA = {"CA-16"})
public class IdentityService {

    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    /**
     * @return Todas las identidades sincronizadas.
     */
    // @Traceability: US-036 - CA-16 (ADR-001 Refactor)
    public List<IdentityEntity> findAll() {
        return identityRepository.findAll();
    }
}
