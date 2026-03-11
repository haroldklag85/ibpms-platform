package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkdeskProjectionRepository extends JpaRepository<WorkdeskProjectionEntity, String> {
    
    // Ejemplos de proyecciones especificas para bandejas filtradas
    Page<WorkdeskProjectionEntity> findByAssignee(String assignee, Pageable pageable);
    Page<WorkdeskProjectionEntity> findByCandidateGroup(String group, Pageable pageable);
}
