package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormCertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FormCertificationRepository extends JpaRepository<FormCertificationEntity, UUID> {
    Optional<FormCertificationEntity> findByFormDefinitionId(UUID formDefinitionId);
}
