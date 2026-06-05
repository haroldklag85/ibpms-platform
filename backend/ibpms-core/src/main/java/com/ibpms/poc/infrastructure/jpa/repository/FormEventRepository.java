// @Traceability: US-029, CA-16
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormEventRepository extends JpaRepository<FormEventEntity, UUID> {
    List<FormEventEntity> findByTaskIdOrderByCreatedAtDesc(String taskId);
    Optional<FormEventEntity> findByIdempotencyKey(UUID idempotencyKey);
}
