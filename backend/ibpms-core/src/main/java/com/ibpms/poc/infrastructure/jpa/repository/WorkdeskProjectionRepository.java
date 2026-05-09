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
    
    // @Traceability: US-036 - CA-05 Privacidad Visual de Colas (Data Segregation Local)
    // CA-14, CA-19, CA-17, CA-01: Strict Tenant Isolation + GIN Index ILIKE + SLA-First Sorting
    // @Traceability(US = "US-001", CA = {"CA-19", "CA-10", "CA-01", "CA-17"})
    // REMEDIACIÓN BUG-S6-002/003/004: Se reemplazó LOWER(w.title) LIKE por ILIKE nativo para 
    // restaurar el uso del índice GIN (pg_trgm) y eliminar Seq Scans.
    // Se corrigió el ORDER BY para que SLA sea la directriz principal (CA-01), y solo el 
    // Impacto Masivo (>= 8) rompa la priorización cronológica (CA-17).
    // Se añadió created_at como criterio de desempate final.
    @Query(value = """
        SELECT * FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND (:search IS NULL OR w.title ILIKE CONCAT('%%', :search, '%%')) 
          AND (:assignee IS NULL OR w.assignee = :assignee) 
        ORDER BY 
          CASE WHEN w.impact_level >= 8 THEN 0 ELSE 1 END ASC, 
          w.sla_expiration_date ASC NULLS LAST, 
          w.created_at ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM ibpms_workdesk_projection w 
        WHERE w.tenant_id = :tenantId 
          AND (:search IS NULL OR w.title ILIKE CONCAT('%%', :search, '%%')) 
          AND (:assignee IS NULL OR w.assignee = :assignee)
        """,
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

    // @Traceability(US = "US-001", CA = {"CA-16", "CA-21", "CA-28"})
    @Query(value = """
        SELECT * FROM ibpms_workdesk_projection w
        WHERE w.tenant_id = :tenantId
          AND w.assignee IS NULL
          AND (:skills IS NULL OR w.category_tag = ANY(CAST(:skills AS VARCHAR[])))
        ORDER BY w.impact_level DESC, w.sla_expiration_date ASC NULLS LAST
        LIMIT 1
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    java.util.Optional<WorkdeskProjectionEntity> findNextAvailableTask(
        @Param("tenantId") String tenantId,
        @Param("skills") String[] skills
    );
}
