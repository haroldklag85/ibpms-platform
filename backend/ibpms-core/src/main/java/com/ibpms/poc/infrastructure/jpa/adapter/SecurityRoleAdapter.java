package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.domain.model.security.SecurityRole;
import com.ibpms.poc.domain.port.out.security.SecurityRolePort;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityRoleAdapter implements SecurityRolePort {

    private final RoleRepository repository;

    public SecurityRoleAdapter(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public SecurityRole saveOrUpdateRole(SecurityRole role) {
        Optional<RoleEntity> existingRole = repository.findByName(role.getName());

        RoleEntity entity = existingRole.orElseGet(() -> {
            RoleEntity newEntity = new RoleEntity();
            newEntity.setId(role.getId() != null ? role.getId() : UUID.randomUUID());
            return newEntity;
        });

        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setProcessDefinitionId(role.getProcessDefinitionId());
        entity.setLaneId(role.getLaneId());
        
        RoleEntity saved = repository.save(entity);

        return new SecurityRole(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                role.getType(), // Keep type from domain if not in Entity
                saved.getProcessDefinitionId(),
                saved.getLaneId());
    }
}
