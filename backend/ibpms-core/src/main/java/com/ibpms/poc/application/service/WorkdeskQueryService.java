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

    public WorkdeskQueryService(WorkdeskProjectionRepository projectionRepository) {
        this.projectionRepository = projectionRepository;
    }

    @Cacheable(value = "workdesk_tasks", key = "#tenantId + '_' + (#effectiveAssignee != null ? #effectiveAssignee : '') + '_' + (#search != null ? #search : '') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<WorkdeskProjectionEntity> getWorkdeskTasks(String tenantId, String search, String effectiveAssignee, Pageable pageable) {
        return projectionRepository.findWorkdeskTasks(tenantId, search, effectiveAssignee, pageable);
    }

    @Cacheable(value = "workdesk_tasks", key = "'facets_' + #tenantId")
    @Transactional(readOnly = true)
    public java.util.List<com.ibpms.poc.application.dto.FacetCountDto> getFacets(String tenantId) {
        return projectionRepository.countByStatusPerTenant(tenantId);
    }
}
