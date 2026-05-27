package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkdeskQueryService {

    private final WorkdeskProjectionRepository projectionRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository delegationRepository;

    public WorkdeskQueryService(WorkdeskProjectionRepository projectionRepository, com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository delegationRepository) {
        this.projectionRepository = projectionRepository;
        this.delegationRepository = delegationRepository;
    }

    @Cacheable(value = "workdesk_tasks", key = "#tenantId + '_' + (#effectiveAssignee != null ? #effectiveAssignee : '') + '_' + (#search != null ? #search : '') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<WorkdeskProjectionEntity> getWorkdeskTasks(String tenantId, String search, String effectiveAssignee, Pageable pageable) {
        java.util.List<String> assignees = null;
        if (effectiveAssignee != null) {
            assignees = new java.util.ArrayList<>();
            assignees.add(effectiveAssignee);
            try {
                java.util.UUID substituteId = java.util.UUID.fromString(effectiveAssignee);
                java.util.List<com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity> delegations = 
                    delegationRepository.findActiveDelegationsForSubstitute(substituteId, java.time.LocalDateTime.now());
                for (com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity d : delegations) {
                    assignees.add(d.getDelegator().getId().toString());
                }
            } catch (Exception e) {
                // Ignore if not a UUID
            }
        }
        // @Traceability: US-001, CA-29 Contadores de Facetas
        String[] assigneesArray = assignees != null ? assignees.toArray(new String[0]) : null;
        return projectionRepository.findWorkdeskTasks(tenantId, search, assigneesArray, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkdeskProjectionEntity> getWorkdeskTasksBySource(String tenantId, String search, String effectiveAssignee, String sourceSystem, Pageable pageable) {
        return projectionRepository.findWorkdeskTasksBySource(tenantId, search, effectiveAssignee, sourceSystem, pageable);
    }

    // @Traceability(US = "US-001", CA = {"CA-29"})
    @Cacheable(value = "workdesk_tasks", key = "'facets_' + #tenantId")
    @Transactional(readOnly = true)
    public java.util.List<com.ibpms.poc.application.dto.FacetCountDto> getFacets(String tenantId) {
        return projectionRepository.countByStatusPerTenant(tenantId);
    }

    // @Traceability: US-001, CA-29 Contadores de Facetas
    @Transactional(readOnly = true)
    public java.util.Map<String, java.util.Map<String, Long>> getFacetsMap(String tenantId) {
        java.util.Map<String, java.util.Map<String, Long>> facetsMap = new java.util.HashMap<>();
        
        // Status facets
        java.util.List<com.ibpms.poc.application.dto.FacetCountDto> statusCounts = projectionRepository.countByStatusPerTenant(tenantId);
        java.util.Map<String, Long> statusMap = new java.util.HashMap<>();
        for (com.ibpms.poc.application.dto.FacetCountDto fc : statusCounts) {
            statusMap.put(fc.getStatus(), fc.getCount());
        }
        facetsMap.put("status", statusMap);
        
        // Origin facets
        java.util.List<com.ibpms.poc.application.dto.FacetCountDto> originCounts = projectionRepository.countBySourceSystemPerTenant(tenantId);
        java.util.Map<String, Long> originMap = new java.util.HashMap<>();
        for (com.ibpms.poc.application.dto.FacetCountDto fc : originCounts) {
            originMap.put(fc.getStatus(), fc.getCount());
        }
        facetsMap.put("origin", originMap);
        
        return facetsMap;
    }
}
