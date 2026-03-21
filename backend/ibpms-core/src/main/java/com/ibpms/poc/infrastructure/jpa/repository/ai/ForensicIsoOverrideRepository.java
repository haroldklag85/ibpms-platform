package com.ibpms.poc.infrastructure.jpa.repository.ai;

import com.ibpms.poc.infrastructure.jpa.entity.ai.ForensicIsoOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Auditoría Legal. No debe exponer rutinas transaccionales de borrado/mutación.
 */
@Repository
public interface ForensicIsoOverrideRepository extends JpaRepository<ForensicIsoOverrideEntity, Long> {
}
