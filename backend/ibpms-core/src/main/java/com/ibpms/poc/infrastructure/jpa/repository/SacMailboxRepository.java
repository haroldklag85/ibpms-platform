package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.SacMailboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SacMailboxRepository extends JpaRepository<SacMailboxEntity, String> {
    Optional<SacMailboxEntity> findByAlias(String alias);

    List<SacMailboxEntity> findByActiveTrue();
}
