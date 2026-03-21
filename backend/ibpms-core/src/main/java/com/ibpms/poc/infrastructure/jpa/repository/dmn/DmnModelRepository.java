package com.ibpms.poc.infrastructure.jpa.repository.dmn;

import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DmnModelRepository extends JpaRepository<DmnModelEntity, String> {
    
    // Recupera los DMN que se quedaron abandonados en estado borrador.
    List<DmnModelEntity> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff);
}
