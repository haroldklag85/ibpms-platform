package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.UserDelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDelegationRepository extends JpaRepository<UserDelegationEntity, UUID> {
    Optional<UserDelegationEntity> findBySupervisorIdAndAssistantIdAndTenantId(String supervisorId, String assistantId, String tenantId);
}
