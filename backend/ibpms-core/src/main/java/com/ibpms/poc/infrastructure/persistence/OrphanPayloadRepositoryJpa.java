package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
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

    public OrphanPayloadRepositoryJpa(SpringDataOrphanPayloadRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public OrphanPayload save(OrphanPayload payload) {
        return springDataRepo.save(payload);
    }

    @Override
    public List<OrphanPayload> findAll() {
        return springDataRepo.findAll();
    }

    @Override
    @Transactional
    public void deleteByCreatedAtBefore(ZonedDateTime cutoff) {
        springDataRepo.deleteByCreatedAtBefore(cutoff);
    }
}

interface SpringDataOrphanPayloadRepo extends JpaRepository<OrphanPayload, UUID> {

    @Modifying
    @Query("DELETE FROM OrphanPayload o WHERE o.createdAt < :cutoff")
    void deleteByCreatedAtBefore(@Param("cutoff") ZonedDateTime cutoff);
}
