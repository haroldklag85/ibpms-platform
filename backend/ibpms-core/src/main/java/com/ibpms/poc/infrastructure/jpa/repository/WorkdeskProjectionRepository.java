package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WorkdeskProjectionRepository extends JpaRepository<WorkdeskProjectionEntity, String> {
    
    // CA-14, CA-19, CA-17: Strict Tenant Isolation + GIN Index ILike + Impact/SLA Sorting
    @Query(value = "SELECT * FROM ibpms_workdesk_projection w WHERE " +
           "w.tenant_id = :tenantId AND " +
           "(:search IS NULL OR w.title ILIKE CONCAT('%', :search, '%')) AND " +
           "(:assignee IS NULL OR w.assignee = :assignee) " +
           "ORDER BY w.impact_level DESC, w.sla_expiration_date ASC NULLS LAST", 
           nativeQuery = true)
    Page<WorkdeskProjectionEntity> findWorkdeskTasks(
           @Param("tenantId") String tenantId, 
           @Param("search") String search, 
           @Param("assignee") String assignee, 
           Pageable pageable);

    // CA-22, CA-29: Faceted Filters & Counters
    @Query("SELECT new com.ibpms.poc.application.dto.FacetCountDto(w.status, COUNT(w)) " +
           "FROM WorkdeskProjectionEntity w WHERE w.tenantId = :tenantId GROUP BY w.status")
    java.util.List<com.ibpms.poc.application.dto.FacetCountDto> countByStatusPerTenant(@Param("tenantId") String tenantId);
}
