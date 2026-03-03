package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BpmnProcessDesignRepository extends JpaRepository<BpmnProcessDesignEntity, UUID> {
    Optional<BpmnProcessDesignEntity> findByTechnicalId(String technicalId);
}
