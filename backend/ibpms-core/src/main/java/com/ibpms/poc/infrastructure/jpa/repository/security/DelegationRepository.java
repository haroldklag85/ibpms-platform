package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DelegationRepository extends JpaRepository<DelegationEntity, UUID> {
    
    // Consulta activa de delegaciones temporales (sustituto)
    @org.springframework.data.jpa.repository.Query("SELECT d FROM DelegationEntity d WHERE d.substitute.id = :substituteId AND d.isActive = true AND :currentTime BETWEEN d.startDate AND d.endDate")
    List<DelegationEntity> findActiveDelegationsForSubstitute(UUID substituteId, LocalDateTime currentTime);
}
