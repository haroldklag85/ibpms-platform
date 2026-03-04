package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.DelegationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DelegationLogRepository extends JpaRepository<DelegationLogEntity, UUID> {

    @Query("SELECT d FROM DelegationLogEntity d WHERE d.isRevoked = false AND d.endDate < :currentTime")
    List<DelegationLogEntity> findExpiredActiveDelegations(LocalDateTime currentTime);
}
