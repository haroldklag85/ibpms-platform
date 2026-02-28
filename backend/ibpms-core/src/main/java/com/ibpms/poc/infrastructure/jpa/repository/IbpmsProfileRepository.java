package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IbpmsProfileRepository extends JpaRepository<IbpmsProfileEntity, UUID> {
    Optional<IbpmsProfileEntity> findByProfileName(String profileName);
}
