package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.FeatureTogglePort;
import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FeatureToggleRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Traceability(US = "US-001", CA = {"CA-08"})
 * POR QUÉ: Adaptador de JPA para Feature Toggles. Encapsula la comunicación con la base de datos 
 * respetando la Arquitectura Hexagonal y aislando el repositorio Spring Data JPA de la capa de aplicación.
 */
@Component
@Traceability(US = "US-001", CA = {"CA-08"})
public class FeatureToggleJpaAdapter implements FeatureTogglePort {

    private final FeatureToggleRepository featureToggleRepository;

    public FeatureToggleJpaAdapter(FeatureToggleRepository featureToggleRepository) {
        this.featureToggleRepository = featureToggleRepository;
    }

    @Override
    public Optional<FeatureToggleEntity> findByTenantIdAndToggleKey(String tenantId, String toggleKey) {
        return featureToggleRepository.findByTenantIdAndToggleKey(tenantId, toggleKey);
    }

    @Override
    public FeatureToggleEntity save(FeatureToggleEntity featureToggle) {
        return featureToggleRepository.save(featureToggle);
    }
}
