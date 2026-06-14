package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.jpa.entity.TenantConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link TenantConfigEntity} instances.
 * Provides data access capabilities for tenant-specific configurations.
 */
@Repository
@Traceability(US = "US-004", CA = {"CA-18"})
public interface TenantConfigRepository extends JpaRepository<TenantConfigEntity, String> {
}
