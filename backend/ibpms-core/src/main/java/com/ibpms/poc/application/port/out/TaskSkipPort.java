package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;

import java.time.LocalDateTime;

public interface TaskSkipPort {
    void save(TaskSkipEntity skipEntity);
    int countRecentSkips(String tenantId, String userId, LocalDateTime since);
}
