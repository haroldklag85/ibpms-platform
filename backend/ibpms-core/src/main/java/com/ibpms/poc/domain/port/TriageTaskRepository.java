// @Traceability: US-003 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.DomainPage;
import com.ibpms.poc.domain.model.TriageTask;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TriageTaskRepository {
    TriageTask save(TriageTask task);
    Optional<TriageTask> findById(UUID id);
    Optional<TriageTask> findByIdForUpdate(UUID id);
    DomainPage<TriageTask> findByStatus(String status, int page, int size);
    void deleteByStatusAndUpdatedAtBefore(String status, ZonedDateTime cutoff);
}
