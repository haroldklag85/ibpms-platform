package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdpGroupMappingRepository extends JpaRepository<IdpGroupMappingEntity, UUID> {
    Optional<IdpGroupMappingEntity> findByIdpGroupId(String idpGroupId);
}
