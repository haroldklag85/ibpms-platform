package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.TriageTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TriageTaskRepository {
    TriageTask save(TriageTask task);
    Optional<TriageTask> findById(UUID id);
    Optional<TriageTask> findByIdForUpdate(UUID id);
    Page<TriageTask> findByStatus(String status, Pageable pageable);
    void deleteByStatusAndUpdatedAtBefore(String status, ZonedDateTime cutoff);
}
