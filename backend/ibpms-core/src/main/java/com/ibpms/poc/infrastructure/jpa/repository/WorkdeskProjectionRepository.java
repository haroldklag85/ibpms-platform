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
    
    // @Traceability: US-036 - CA-23 Delegación In-Flight + CA-05 Privacidad Visual de Colas
    // CA-14, CA-19, CA-17, CA-01: Strict Tenant Isolation + GIN Index ILIKE + SLA-First Sorting
    // REMEDIACIÓN BUG-S6-002/003/004: ILIKE nativo para GIN (pg_trgm), elimina Seq Scans.
    // SLA-First sorting (CA-01) + Impacto Masivo >= 8 (CA-17) + created_at desempate.
    // @Traceability(US="US-036", CA={"CA-23", "BUG-PG-CAST"}, DESC="Híbrido: Delegación In-Flight con Safe Postgres Casting para prevenir PSQLException en nulos")
    // MERGE: Se conserva soporte a List<assignees> (origin/DevDavid) y protección CAST de nulos nativos (HEAD).
    // @Traceability: US-005, CA-78 Sandbox Multi-tenancy (Exclude sandbox_tenant from inbox)
    @Query(value = """
        SELECT * FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND w.tenant_id != 'sandbox_tenant'
          AND (CAST(:search AS VARCHAR) IS NULL OR w.title ILIKE CONCAT('%%', CAST(:search AS VARCHAR), '%%')) 
          AND (
            (CAST(:view AS VARCHAR) = 'POOL' AND w.assignee IS NULL) OR
            (CAST(:view AS VARCHAR) = 'PERSONAL' AND (CAST(:assignees AS VARCHAR[]) IS NULL OR w.assignee = ANY(CAST(:assignees AS VARCHAR[])))) OR
            (CAST(:view AS VARCHAR) IS NULL AND (CAST(:assignees AS VARCHAR[]) IS NULL OR w.assignee = ANY(CAST(:assignees AS VARCHAR[])) OR w.assignee IS NULL))
          )
        ORDER BY 
          CASE WHEN w.impact_level >= 8 THEN 0 ELSE 1 END ASC, 
          w.sla_expiration_date ASC NULLS LAST, 
          w.id ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND w.tenant_id != 'sandbox_tenant'
          AND (CAST(:search AS VARCHAR) IS NULL OR w.title ILIKE CONCAT('%%', CAST(:search AS VARCHAR), '%%')) 
          AND (
            (CAST(:view AS VARCHAR) = 'POOL' AND w.assignee IS NULL) OR
            (CAST(:view AS VARCHAR) = 'PERSONAL' AND (CAST(:assignees AS VARCHAR[]) IS NULL OR w.assignee = ANY(CAST(:assignees AS VARCHAR[])))) OR
            (CAST(:view AS VARCHAR) IS NULL AND (CAST(:assignees AS VARCHAR[]) IS NULL OR w.assignee = ANY(CAST(:assignees AS VARCHAR[])) OR w.assignee IS NULL))
          )
        """,
        nativeQuery = true)
    // @Traceability: US-001, CA-29 Contadores de Facetas
    Page<WorkdeskProjectionEntity> findWorkdeskTasks(
           @Param("tenantId") String tenantId, 
           @Param("search") String search, 
           @Param("assignees") String[] assignees, 
           @Param("view") String view,
           Pageable pageable);

    @Query(value = """
        SELECT * FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND w.tenant_id != 'sandbox_tenant'
          AND (CAST(:search AS VARCHAR) IS NULL OR w.title ILIKE CONCAT('%%', CAST(:search AS VARCHAR), '%%')) 
          AND (CAST(:assignee AS VARCHAR) IS NULL OR w.assignee = CAST(:assignee AS VARCHAR) OR w.assignee IS NULL) 
          AND w.source_system = :sourceSystem
        ORDER BY 
          CASE WHEN w.impact_level >= 8 THEN 0 ELSE 1 END ASC, 
          w.sla_expiration_date ASC NULLS LAST, 
          w.id ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND w.tenant_id != 'sandbox_tenant'
          AND (CAST(:search AS VARCHAR) IS NULL OR w.title ILIKE CONCAT('%%', CAST(:search AS VARCHAR), '%%')) 
          AND (CAST(:assignee AS VARCHAR) IS NULL OR w.assignee = CAST(:assignee AS VARCHAR) OR w.assignee IS NULL)
          AND w.source_system = :sourceSystem
        """,
        nativeQuery = true)
    Page<WorkdeskProjectionEntity> findWorkdeskTasksBySource(
           @Param("tenantId") String tenantId, 
           @Param("search") String search, 
           @Param("assignee") String assignee, 
           @Param("sourceSystem") String sourceSystem,
           Pageable pageable);

    // CA-22, CA-29: Faceted Filters & Counters
    @Query("SELECT new com.ibpms.poc.application.dto.FacetCountDto(w.status, COUNT(w)) " +
           "FROM WorkdeskProjectionEntity w WHERE w.tenantId = :tenantId AND w.tenantId != 'sandbox_tenant' GROUP BY w.status")
    java.util.List<com.ibpms.poc.application.dto.FacetCountDto> countByStatusPerTenant(@Param("tenantId") String tenantId);

    // @Traceability: US-001, CA-29 Contadores de Facetas
    @Query("SELECT new com.ibpms.poc.application.dto.FacetCountDto(w.sourceSystem, COUNT(w)) " +
           "FROM WorkdeskProjectionEntity w WHERE w.tenantId = :tenantId AND w.tenantId != 'sandbox_tenant' GROUP BY w.sourceSystem")
    java.util.List<com.ibpms.poc.application.dto.FacetCountDto> countBySourceSystemPerTenant(@Param("tenantId") String tenantId);

    // @Traceability(US = "US-001", CA = {"CA-16", "CA-21", "CA-28"})
    @Query(value = """
        SELECT * FROM ibpms_workdesk_projection w
        WHERE w.tenant_id = :tenantId
          AND w.tenant_id != 'sandbox_tenant'
          AND w.assignee IS NULL
          AND (CAST(:skills AS VARCHAR[]) IS NULL OR w.category_tag = ANY(CAST(:skills AS VARCHAR[])))
        ORDER BY w.impact_level DESC, w.sla_expiration_date ASC NULLS LAST
        LIMIT 1
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    java.util.Optional<WorkdeskProjectionEntity> findNextAvailableTask(
        @Param("tenantId") String tenantId,
        @Param("skills") String[] skills
    );
}
