package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.TimeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, UUID> {
    List<TimeLogEntity> findByReferenceId(UUID referenceId);
    Optional<TimeLogEntity> findByUserIdAndReferenceIdAndStoppedAtIsNull(String userId, UUID referenceId);
}
