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
    
    // Búsqueda dinámica tolerante a null para search keyword y usuario delegado
    @Query("SELECT w FROM WorkdeskProjectionEntity w WHERE " +
           "(:search IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.originalTaskId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:delegatedUserId IS NULL OR w.assignee = :delegatedUserId)")
    Page<WorkdeskProjectionEntity> findByCombinedSearch(@Param("search") String search, 
                                                        @Param("delegatedUserId") String delegatedUserId, 
                                                        Pageable pageable);

    // Ejemplos de proyecciones especificas para bandejas filtradas
    Page<WorkdeskProjectionEntity> findByAssignee(String assignee, Pageable pageable);
    Page<WorkdeskProjectionEntity> findByCandidateGroup(String group, Pageable pageable);
}
