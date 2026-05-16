package com.ibpms.poc.application.ports.out;

import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Optional;

/**
 * @Traceability(US = "US-001", CA = {"CA-08"})
 * POR QUÉ: Puerto de salida creado para aislar la persistencia de Feature Toggles, 
 * removiendo el acoplamiento directo entre el controlador web y la base de datos (JPA).
 */
@Traceability(US = "US-001", CA = {"CA-08"})
public interface FeatureTogglePort {
    Optional<FeatureToggleEntity> findByTenantIdAndToggleKey(String tenantId, String toggleKey);
    FeatureToggleEntity save(FeatureToggleEntity featureToggle);
}
