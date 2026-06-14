package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.jpa.entity.FormEventStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing {@link FormEventStoreEntity}.
 * Serves as the primary data access component for the CQRS event store.
 */
@Repository
@Traceability(US = "US-017", CA = {"CA-06"})
public interface FormEventStoreRepository extends JpaRepository<FormEventStoreEntity, UUID> {
}
