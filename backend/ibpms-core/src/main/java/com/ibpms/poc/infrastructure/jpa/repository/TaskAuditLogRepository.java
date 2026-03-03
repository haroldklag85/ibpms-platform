package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.TaskAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskAuditLogRepository extends JpaRepository<TaskAuditLogEntity, UUID> {
    long countByTaskIdAndAction(String taskId, String action);
}
