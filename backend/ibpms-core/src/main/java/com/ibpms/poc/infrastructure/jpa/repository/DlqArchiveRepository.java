package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DlqArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface DlqArchiveRepository extends JpaRepository<DlqArchiveEntity, String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DlqArchiveEntity e WHERE e.archivedAt < :threshold")
    void deleteOlderThan(LocalDateTime threshold);
}
