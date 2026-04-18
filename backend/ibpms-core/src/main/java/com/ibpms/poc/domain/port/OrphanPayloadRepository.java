package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.OrphanPayload;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Hexagonal port for orphan/rejected payload persistence (US-004 CA-3, CA-11, CA-13).
 */
public interface OrphanPayloadRepository {
    OrphanPayload save(OrphanPayload payload);
    List<OrphanPayload> findAll();
    void deleteByCreatedAtBefore(ZonedDateTime cutoff);
}
