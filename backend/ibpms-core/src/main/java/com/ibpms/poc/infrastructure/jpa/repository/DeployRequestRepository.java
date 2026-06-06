package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DeployRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeployRequestRepository extends JpaRepository<DeployRequestEntity, UUID> {
    List<DeployRequestEntity> findByProcessDefinitionKeyOrderByRequestedAtDesc(String processDefinitionKey);
}
