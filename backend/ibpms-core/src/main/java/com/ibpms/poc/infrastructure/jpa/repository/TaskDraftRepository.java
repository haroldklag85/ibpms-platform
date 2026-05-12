package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.domain.entity.TaskDraftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link TaskDraftEntity}.
 * Allows persisting and retrieving user task drafts.
 */
@Repository
@Traceability(US = "US-017", CA = {"CA-06"})
public interface TaskDraftRepository extends JpaRepository<TaskDraftEntity, UUID> {
    Optional<TaskDraftEntity> findByTaskIdAndUserId(String taskId, String userId);
}
