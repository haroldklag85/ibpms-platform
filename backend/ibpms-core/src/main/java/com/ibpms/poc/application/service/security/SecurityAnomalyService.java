package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAnomalyEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.SecurityAnomalyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class SecurityAnomalyService {

    private final SecurityAnomalyRepository repository;

    public SecurityAnomalyService(SecurityAnomalyRepository repository) {
        this.repository = repository;
    }

    public List<SecurityAnomalyEntity> getAnomaliesByStatus(String status) {
        return repository.findByStatusOrderByTimestampDesc(status != null ? status : "OPEN");
    }

    public SecurityAnomalyEntity resolveAnomaly(UUID anomalyId, String resolverUserId) {
        SecurityAnomalyEntity anomaly = repository.findById(anomalyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anomalía de Seguridad Forense no encontrada."));

        if (!"OPEN".equals(anomaly.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La Anomalía ya se encuentra procesada.");
        }

        anomaly.setStatus("RESOLVED");
        anomaly.setResolvedBy(resolverUserId);
        anomaly.setResolvedAt(LocalDateTime.now());

        return repository.save(anomaly);
    }
}
