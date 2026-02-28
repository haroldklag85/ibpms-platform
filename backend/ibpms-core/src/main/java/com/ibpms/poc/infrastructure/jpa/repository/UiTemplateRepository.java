package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.UiTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UiTemplateRepository extends JpaRepository<UiTemplateEntity, String> {

    Optional<UiTemplateEntity> findFirstByNameOrderByCreatedAtDesc(String name);

    List<UiTemplateEntity> findAllByNameOrderByCreatedAtDesc(String name);

    boolean existsByNameAndVersion(String name, String version);
}
