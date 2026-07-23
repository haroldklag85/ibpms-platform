// @Traceability: US-005/US-036 - ADR-001
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.bpmn.BpmnLaneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BpmnLaneJpaRepository extends JpaRepository<BpmnLaneEntity, UUID> {
    List<BpmnLaneEntity> findByProcessDesign_TechnicalId(String technicalId);
    void deleteByProcessDesign_Id(UUID processDesignId);
}
