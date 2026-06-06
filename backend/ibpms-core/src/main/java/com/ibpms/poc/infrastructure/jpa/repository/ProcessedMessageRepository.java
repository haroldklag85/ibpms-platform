package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, UUID> {
    Optional<ProcessedMessageEntity> findByIdempotencyKey(UUID idempotencyKey);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProcessedMessageEntity e WHERE e.processedAt < :threshold")
    void deleteOlderThan(LocalDateTime threshold);
}
