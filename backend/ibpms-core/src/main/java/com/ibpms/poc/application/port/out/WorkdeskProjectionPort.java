package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;

import java.util.Optional;

public interface WorkdeskProjectionPort {
    Optional<WorkdeskProjectionEntity> findNextAvailableTask(String tenantId, String[] skills);
    Optional<WorkdeskProjectionEntity> findById(String id);
    void save(WorkdeskProjectionEntity entity);
}
