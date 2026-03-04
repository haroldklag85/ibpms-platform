package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleTemplateRepository extends JpaRepository<RoleTemplateEntity, UUID> {
    Optional<RoleTemplateEntity> findByRoleName(String roleName);
}
