package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchyEntity, UUID> {
    List<RoleHierarchyEntity> findByParentRoleId(UUID parentRoleId);
}
