package com.ibpms.poc.infrastructure.jpa.repository.dmn;

import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Repositorio JPA para modelos DMN.
 * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
 */
@Repository
@Traceability(US = "US-007", CA = {"CA-13"})
public interface DmnModelRepository extends JpaRepository<DmnModelEntity, String> {
    
    // Recupera los DMN que se quedaron abandonados en estado borrador.
    List<DmnModelEntity> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff);
    
    // Lista los DMN por tenant
    List<DmnModelEntity> findByTenantId(String tenantId);
}
