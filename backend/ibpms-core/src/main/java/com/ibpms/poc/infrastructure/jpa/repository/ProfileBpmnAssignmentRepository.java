package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileBpmnAssignmentRepository extends JpaRepository<ProfileBpmnAssignmentEntity, UUID> {
    List<ProfileBpmnAssignmentEntity> findByProfile_Id(UUID profileId);
    // @Traceability: US-005, CA-06 Purga de Roles Zombies
    List<ProfileBpmnAssignmentEntity> findByBpmnProcessKey(String bpmnProcessKey);
}
