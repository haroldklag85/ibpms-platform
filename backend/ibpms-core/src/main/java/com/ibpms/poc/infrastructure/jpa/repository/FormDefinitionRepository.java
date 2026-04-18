package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormDefinitionRepository extends JpaRepository<FormDefinitionEntity, UUID> {
    List<FormDefinitionEntity> findByFormIdOrderByVersionIdDesc(UUID formId);
    Optional<FormDefinitionEntity> findByFormIdAndVersionId(UUID formId, Integer versionId);
}
