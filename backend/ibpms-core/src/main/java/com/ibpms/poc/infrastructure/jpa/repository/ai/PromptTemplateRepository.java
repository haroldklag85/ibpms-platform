package com.ibpms.poc.infrastructure.jpa.repository.ai;

import com.ibpms.poc.infrastructure.jpa.entity.ai.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    Optional<PromptTemplateEntity> findByName(String name);
}
