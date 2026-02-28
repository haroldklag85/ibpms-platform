package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboundWebhookRepository extends JpaRepository<InboundWebhookEntity, UUID> {
    Optional<InboundWebhookEntity> findByIdAndIsActiveTrue(UUID id);
}
