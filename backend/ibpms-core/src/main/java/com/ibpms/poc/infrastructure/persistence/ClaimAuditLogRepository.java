package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimAuditLogRepository extends JpaRepository<ClaimAuditLog, UUID> {
    List<ClaimAuditLog> findByTaskIdOrderByTimestampDesc(UUID taskId);
}
