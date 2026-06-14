package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.agile.AgileTask;
import java.util.Optional;
import java.util.UUID;

public interface AgileTaskPort {
    Optional<AgileTask> findById(UUID taskId);
    Optional<AgileTask> findByIdForUpdate(UUID taskId);
    AgileTask save(AgileTask task);
}
