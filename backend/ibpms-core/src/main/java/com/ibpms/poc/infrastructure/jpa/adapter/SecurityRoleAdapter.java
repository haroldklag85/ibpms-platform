package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.domain.model.security.SecurityRole;
import com.ibpms.poc.domain.port.out.security.SecurityRolePort;
import com.ibpms.poc.infrastructure.jpa.entity.SysRoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SysRoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityRoleAdapter implements SecurityRolePort {

    private final SysRoleRepository repository;

    public SecurityRoleAdapter(SysRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public SecurityRole saveOrUpdateRole(SecurityRole role) {
        Optional<SysRoleEntity> existingRole = repository.findByName(role.getName());

        SysRoleEntity entity = existingRole.orElseGet(() -> {
            SysRoleEntity newEntity = new SysRoleEntity();
            newEntity.setId(role.getId() != null ? role.getId() : UUID.randomUUID());
            return newEntity;
        });

        entity.setName(role.getName());
        entity.setType(role.getType());
        entity.setProcessDefinitionId(role.getProcessDefinitionId());
        entity.setLaneId(role.getLaneId());

        SysRoleEntity saved = repository.save(entity);

        return new SecurityRole(
                saved.getId(),
                saved.getName(),
                null, // description no se guarda aún en este DTO
                saved.getType(),
                saved.getProcessDefinitionId(),
                saved.getLaneId());
    }
}
