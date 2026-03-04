package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {
    Optional<ApiKeyEntity> findByClientName(String clientName);
}
