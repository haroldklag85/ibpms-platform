package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.ExpedienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<ExpedienteEntity, String> {
    Optional<ExpedienteEntity> findByBusinessKey(String businessKey);
}
