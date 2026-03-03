package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormDesignRepository extends JpaRepository<FormDesignEntity, UUID> {

    Optional<FormDesignEntity> findTopByTechnicalNameOrderByVersionDesc(String technicalName);

    Optional<FormDesignEntity> findByTechnicalNameAndVersion(String technicalName, Integer version);
}
