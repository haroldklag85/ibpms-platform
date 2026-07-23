package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureToggleRepository extends JpaRepository<FeatureToggleEntity, UUID> {
    Optional<FeatureToggleEntity> findByTenantIdAndToggleKey(String tenantId, String toggleKey);
}
