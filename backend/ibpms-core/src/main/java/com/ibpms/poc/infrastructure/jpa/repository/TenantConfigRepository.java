package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.jpa.entity.TenantConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Traceability(US = "US-004", CA = {"CA-18"})
public interface TenantConfigRepository extends JpaRepository<TenantConfigEntity, String> {
}
