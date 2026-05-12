package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.TaskSkipPort;
import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TaskSkipRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskSkipJpaAdapter implements TaskSkipPort {

    private final TaskSkipRepository repository;

    public TaskSkipJpaAdapter(TaskSkipRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(TaskSkipEntity skipEntity) {
        repository.save(skipEntity);
    }

    @Override
    public int countRecentSkips(String tenantId, String userId, LocalDateTime since) {
        return repository.countRecentSkips(tenantId, userId, since);
    }
}
