package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FeatureToggleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeatureToggleService {

    private final FeatureToggleRepository toggleRepository;

    public FeatureToggleService(FeatureToggleRepository toggleRepository) {
        this.toggleRepository = toggleRepository;
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String tenantId, String toggleKey) {
        return toggleRepository.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .map(FeatureToggleEntity::getEnabled)
                .orElse(false);
    }

    @Transactional
    public void setFeatureToggle(String tenantId, String toggleKey, boolean enabled, String changedBy) {
        FeatureToggleEntity entity = toggleRepository.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .orElseGet(() -> {
                    FeatureToggleEntity newToggle = new FeatureToggleEntity();
                    newToggle.setTenantId(tenantId);
                    newToggle.setToggleKey(toggleKey);
                    return newToggle;
                });

        entity.setEnabled(enabled);
        entity.setChangedBy(changedBy);
        entity.setChangedAt(LocalDateTime.now());

        toggleRepository.save(entity);
    }
}
