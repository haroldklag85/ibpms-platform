// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.BpmnAuditPort;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Component
public class BpmnAuditJpaAdapter implements BpmnAuditPort {

    private final BpmnDesignAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public BpmnAuditJpaAdapter(BpmnDesignAuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void logAction(UUID processDesignId, String action, String userId, int versionAffected, String details) {
        BpmnDesignAuditLogEntity.Action enumAction;
        try {
            enumAction = BpmnDesignAuditLogEntity.Action.valueOf(action);
        } catch (IllegalArgumentException e) {
            enumAction = BpmnDesignAuditLogEntity.Action.PRE_FLIGHT; // default or handle
        }
        
        Map<String, Object> detailsMap = null;
        if (details != null && !details.isBlank()) {
            try {
                detailsMap = objectMapper.readValue(details, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // ignore or log
            }
        }

        BpmnDesignAuditLogEntity entity = new BpmnDesignAuditLogEntity(
            processDesignId,
            enumAction,
            userId,
            versionAffected,
            detailsMap
        );
        repository.save(entity);
    }
}
