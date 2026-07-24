// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.BpmnDesignPort;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Adaptador JPA para la persistencia de procesos BPMN.
 * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
 */
@Component
@Traceability(US = "US-005", CA = {"CA-01"})
public class BpmnDesignJpaAdapter implements BpmnDesignPort {

    private final BpmnProcessDesignRepository repository;
    private final ObjectMapper objectMapper;

    public BpmnDesignJpaAdapter(BpmnProcessDesignRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<BpmnProcessDesign> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<BpmnProcessDesign> findByTechnicalId(String technicalId) {
        return repository.findByTechnicalId(technicalId).map(this::toDomain);
    }

    @Override
    public BpmnProcessDesign save(BpmnProcessDesign design) {
        BpmnProcessDesignEntity entity = toEntity(design);
        BpmnProcessDesignEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public java.util.List<BpmnProcessDesign> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }

    private BpmnProcessDesign toDomain(BpmnProcessDesignEntity entity) {
        BpmnProcessDesign domain = BpmnProcessDesign.reconstituir(
                entity.getId(),
                entity.getName(),
                entity.getTechnicalId(),
                entity.getFormPattern() != null ? BpmnProcessDesign.FormPattern.valueOf(entity.getFormPattern().name()) : null,
                entity.getStatus() != null ? BpmnProcessDesign.Status.valueOf(entity.getStatus().name()) : BpmnProcessDesign.Status.DRAFT,
                entity.getCurrentVersion(),
                entity.getLockedBy(),
                entity.getLockedAt(),
                entity.getXmlDraft(),
                entity.getMaxNodes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy()
        );
        String whitelistStr = null;
        if (entity.getGenericFormWhitelist() != null) {
            try {
                whitelistStr = objectMapper.writeValueAsString(entity.getGenericFormWhitelist());
            } catch (Exception e) {
                // ignore or log
            }
        }
        domain.updateGenericFormConfig(whitelistStr);
        return domain;
    }

    private BpmnProcessDesignEntity toEntity(BpmnProcessDesign domain) {
        BpmnProcessDesignEntity entity = null;
        if (domain.getId() != null) {
            entity = repository.findById(domain.getId()).orElse(null);
        }
        if (entity == null) {
            entity = new BpmnProcessDesignEntity();
            entity.setId(domain.getId());
        }
        
        entity.setName(domain.getName());
        entity.setTechnicalId(domain.getTechnicalId());
        entity.setFormPattern(domain.getFormPattern() != null ? BpmnProcessDesignEntity.FormPattern.valueOf(domain.getFormPattern().name()) : null);
        entity.setStatus(domain.getStatus() != null ? BpmnProcessDesignEntity.Status.valueOf(domain.getStatus().name()) : null);
        entity.setCurrentVersion(domain.getCurrentVersion());
        entity.setLockedBy(domain.getLockedBy());
        entity.setLockedAt(domain.getLockedAt());
        entity.setXmlDraft(domain.getXmlDraft());
        entity.setMaxNodes(domain.getMaxNodes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        Map<String, Object> whitelistMap = null;
        if (domain.getGenericFormWhitelist() != null && !domain.getGenericFormWhitelist().isBlank()) {
            try {
                whitelistMap = objectMapper.readValue(domain.getGenericFormWhitelist(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // ignore
            }
        }
        entity.setGenericFormWhitelist(whitelistMap);
        // optLockVersion is managed by JPA
        return entity;
    }
}
