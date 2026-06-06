package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleDelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoleDelegationRepository extends JpaRepository<RoleDelegationEntity, UUID> {

    @Query("SELECT d FROM RoleDelegationEntity d WHERE d.delegateId = :delegateId AND d.active = true AND :now BETWEEN d.startDate AND d.endDate")
    List<RoleDelegationEntity> findActiveDelegationsForDelegate(UUID delegateId, LocalDateTime now);
}
