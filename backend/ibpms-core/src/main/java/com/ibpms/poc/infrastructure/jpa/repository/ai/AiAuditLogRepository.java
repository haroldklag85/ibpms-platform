package com.ibpms.poc.infrastructure.jpa.repository.ai;

import com.ibpms.poc.infrastructure.jpa.entity.ai.AiAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiAuditLogRepository extends JpaRepository<AiAuditLogEntity, UUID> {
    List<AiAuditLogEntity> findByExecutionIdOrderByCreatedAtDesc(String executionId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM AiAuditLogEntity a WHERE a.eventType = :eventType")
    long countByEventType(@org.springframework.data.repository.query.Param("eventType") String eventType);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(AVG(a.similarityScore), 0.0) FROM AiAuditLogEntity a")
    double getAverageSimilarityScore();
}
