// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.WebhookTransaction;
import com.ibpms.poc.domain.port.WebhookTransactionRepository;
import com.ibpms.poc.infrastructure.jpa.entity.WebhookTransactionJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.WebhookTransactionMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WebhookTransactionRepositoryJpa implements WebhookTransactionRepository {

    private final SpringDataWebhookTransactionRepo springDataRepo;
    private final WebhookTransactionMapper mapper;

    public WebhookTransactionRepositoryJpa(SpringDataWebhookTransactionRepo springDataRepo, WebhookTransactionMapper mapper) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
    }

    @Override
    public WebhookTransaction save(WebhookTransaction transaction) {
        WebhookTransactionJpaEntity entity = mapper.toEntity(transaction);
        WebhookTransactionJpaEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WebhookTransaction> findByMessageId(String messageId) {
        return springDataRepo.findByMessageId(messageId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByMessageId(String messageId) {
        return springDataRepo.existsByMessageId(messageId);
    }
}

interface SpringDataWebhookTransactionRepo extends JpaRepository<WebhookTransactionJpaEntity, UUID> {
    Optional<WebhookTransactionJpaEntity> findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
}
