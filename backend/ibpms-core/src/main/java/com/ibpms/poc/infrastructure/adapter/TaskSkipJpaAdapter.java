// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.TaskSkipPort;
import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TaskSkipRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador JPA para el puerto de omisión de tareas (TaskSkip).
 * Encapsula la persistencia física de las razones de rechazo.
 */
@Component
@Traceability(US = "US-001", CA = {"CA-21"})
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
