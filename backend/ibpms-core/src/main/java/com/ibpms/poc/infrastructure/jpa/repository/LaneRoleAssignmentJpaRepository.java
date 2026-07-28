// @Traceability: US-005/US-036 - ADR-001
package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.security.LaneRoleAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LaneRoleAssignmentJpaRepository extends JpaRepository<LaneRoleAssignmentEntity, UUID> {
    List<LaneRoleAssignmentEntity> findByRole_Id(UUID roleId);
    List<LaneRoleAssignmentEntity> findByLane_Id(UUID laneId);
    void deleteByRole_Id(UUID roleId);
}
