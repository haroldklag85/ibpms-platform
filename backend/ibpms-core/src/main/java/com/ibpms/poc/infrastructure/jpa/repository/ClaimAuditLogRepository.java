package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.ClaimAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimAuditLogRepository extends JpaRepository<ClaimAuditLogEntity, UUID> {
    List<ClaimAuditLogEntity> findByTaskIdOrderByTimestampDesc(UUID taskId);
}
