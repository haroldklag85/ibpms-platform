package com.ibpms.poc.application.service;

import com.ibpms.poc.application.ports.in.UpdateFeatureToggleUseCase;
import com.ibpms.poc.application.ports.out.FeatureTogglePort;
import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @Traceability(US = "US-001", CA = {"CA-08"})
 * POR QUÉ: Implementación del caso de uso de Feature Toggles que orquesta 
 * la consulta y persistencia a través de los puertos (Hexagonal), garantizando 
 * aislamiento entre la lógica transaccional y la capa Web/DB.
 */
@Service
@Traceability(US = "US-001", CA = {"CA-08"})
public class FeatureToggleService implements UpdateFeatureToggleUseCase {

    private final FeatureTogglePort featureTogglePort;

    public FeatureToggleService(FeatureTogglePort featureTogglePort) {
        this.featureTogglePort = featureTogglePort;
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String tenantId, String toggleKey) {
        return featureTogglePort.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .map(FeatureToggleEntity::getEnabled)
                .orElse(false);
    }

    @Transactional
    public void setFeatureToggle(String tenantId, String toggleKey, boolean enabled, String changedBy) {
        FeatureToggleEntity entity = featureTogglePort.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .orElseGet(() -> {
                    FeatureToggleEntity newToggle = new FeatureToggleEntity();
                    newToggle.setTenantId(tenantId);
                    newToggle.setToggleKey(toggleKey);
                    return newToggle;
                });

        entity.setEnabled(enabled);
        entity.setChangedBy(changedBy);
        entity.setChangedAt(LocalDateTime.now());

        featureTogglePort.save(entity);
    }

    @Override
    @Transactional
    public boolean updateFeatureToggle(String tenantId, String toggleKey, Boolean enabled) {
        FeatureToggleEntity toggle = featureTogglePort.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .orElseGet(() -> {
                    FeatureToggleEntity newToggle = new FeatureToggleEntity();
                    newToggle.setTenantId(tenantId);
                    newToggle.setToggleKey(toggleKey);
                    return newToggle;
                });
        
        toggle.setEnabled(enabled != null ? enabled : false);
        featureTogglePort.save(toggle);
        return toggle.getEnabled();
    }
}
