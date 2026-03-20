package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleAuditLogRepository extends JpaRepository<RoleAuditLogEntity, UUID> {
    List<RoleAuditLogEntity> findByRoleIdOrderByTimestampDesc(UUID roleId);
}
