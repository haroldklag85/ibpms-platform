package com.ibpms.poc.infrastructure.jpa.repository.integration;

import com.ibpms.poc.infrastructure.jpa.entity.integration.ApiConnectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiConnectorRepository extends JpaRepository<ApiConnectorEntity, UUID> {

    Optional<ApiConnectorEntity> findBySystemCodeAndVersion(String systemCode, String version);
}
