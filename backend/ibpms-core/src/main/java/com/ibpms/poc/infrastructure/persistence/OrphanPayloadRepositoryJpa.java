// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import com.ibpms.poc.infrastructure.jpa.entity.OrphanPayloadJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.OrphanPayloadMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class OrphanPayloadRepositoryJpa implements OrphanPayloadRepository {

    private final SpringDataOrphanPayloadRepo springDataRepo;
    private final OrphanPayloadMapper mapper;

    public OrphanPayloadRepositoryJpa(SpringDataOrphanPayloadRepo springDataRepo, OrphanPayloadMapper mapper) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
    }

    @Override
    public OrphanPayload save(OrphanPayload payload) {
        OrphanPayloadJpaEntity entity = mapper.toEntity(payload);
        OrphanPayloadJpaEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<OrphanPayload> findAll() {
        return springDataRepo.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByCreatedAtBefore(ZonedDateTime cutoff) {
        springDataRepo.deleteByCreatedAtBefore(cutoff);
    }
}

interface SpringDataOrphanPayloadRepo extends JpaRepository<OrphanPayloadJpaEntity, UUID> {

    @Modifying
    @Query("DELETE FROM OrphanPayloadJpaEntity o WHERE o.createdAt < :cutoff")
    void deleteByCreatedAtBefore(@Param("cutoff") ZonedDateTime cutoff);
}
