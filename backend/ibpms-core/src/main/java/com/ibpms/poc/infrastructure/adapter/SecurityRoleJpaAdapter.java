// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.SecurityRolePort;
import com.ibpms.poc.domain.model.security.SecurityRole;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityRoleJpaAdapter implements SecurityRolePort {

    private final RoleRepository repository;

    public SecurityRoleJpaAdapter(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SecurityRole> findByIsVipRestrictedTrue() {
        return repository.findByIsVipRestrictedTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private SecurityRole toDomain(RoleEntity entity) {
        return new SecurityRole(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSource() != null ? entity.getSource() : "GLOBAL",
                entity.getProcessDefinitionId(),
                entity.getLaneId()
        );
    }
}
