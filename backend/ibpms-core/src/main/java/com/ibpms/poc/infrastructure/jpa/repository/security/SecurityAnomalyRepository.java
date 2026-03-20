package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAnomalyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityAnomalyRepository extends JpaRepository<SecurityAnomalyEntity, UUID> {
    
    List<SecurityAnomalyEntity> findByStatusOrderByTimestampDesc(String status);

}
