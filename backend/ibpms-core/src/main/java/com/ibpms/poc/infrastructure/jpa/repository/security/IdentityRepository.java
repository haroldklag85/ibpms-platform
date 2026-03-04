package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.IdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityRepository extends JpaRepository<IdentityEntity, UUID> {
    Optional<IdentityEntity> findByEntraIdObjectId(String entraIdObjectId);

    Optional<IdentityEntity> findByEmail(String email);
}
