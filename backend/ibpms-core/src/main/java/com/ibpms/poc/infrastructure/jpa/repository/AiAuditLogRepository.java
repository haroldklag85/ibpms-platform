package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.AiAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiAuditLogRepository extends JpaRepository<AiAuditLogEntity, UUID> {

    long count();

    long countByEventType(String eventType);

    @Query("SELECT AVG(a.similarityScore) FROM AiAuditLogEntity a WHERE a.similarityScore IS NOT NULL")
    Double getAverageSimilarityScore();
}
