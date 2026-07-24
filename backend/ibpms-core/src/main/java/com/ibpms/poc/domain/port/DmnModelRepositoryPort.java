// @Traceability: US-007 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.DmnModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DmnModelRepositoryPort {
    Optional<DmnModel> findById(String id);
    DmnModel save(DmnModel dmnModel);
    void delete(DmnModel dmnModel);
    List<DmnModel> findByTenantId(String tenantId);
    List<DmnModel> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff);
}
