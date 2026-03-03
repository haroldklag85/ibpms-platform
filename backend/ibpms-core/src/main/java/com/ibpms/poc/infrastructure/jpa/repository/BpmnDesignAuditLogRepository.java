package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BpmnDesignAuditLogRepository extends JpaRepository<BpmnDesignAuditLogEntity, UUID> {
    List<BpmnDesignAuditLogEntity> findByProcessDesignIdOrderByTimestampDesc(UUID processDesignId);
}
