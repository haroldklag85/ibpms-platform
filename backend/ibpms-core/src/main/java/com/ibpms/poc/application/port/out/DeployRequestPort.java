package com.ibpms.poc.application.port.out;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface DeployRequestPort {
    Optional<DeployRequestInfo> findById(UUID id);
    DeployRequestInfo save(DeployRequestInfo deployRequestInfo);
    List<DeployRequestInfo> findByProcessKey(String processKey);

    record DeployRequestInfo(
        UUID id,
        String processDefinitionKey,
        String requestedBy,
        java.time.LocalDateTime requestedAt,
        String status,
        String reviewedBy,
        java.time.LocalDateTime reviewedAt,
        String reviewComment,
        String xmlPayload
    ) {}
}
