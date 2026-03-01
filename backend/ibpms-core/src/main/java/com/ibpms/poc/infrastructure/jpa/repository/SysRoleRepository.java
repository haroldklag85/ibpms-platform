package com.ibpms.poc.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibpms.poc.infrastructure.jpa.entity.SysRoleEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRoleEntity, UUID> {

    /**
     * Busca un rol por su nombre único para las validaciones idempotentes.
     */
    Optional<SysRoleEntity> findByName(String name);
}
