package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.WebhookTransaction;
import com.ibpms.poc.domain.port.WebhookTransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WebhookTransactionRepositoryJpa implements WebhookTransactionRepository {

    private final SpringDataWebhookTransactionRepo springDataRepo;

    public WebhookTransactionRepositoryJpa(SpringDataWebhookTransactionRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public WebhookTransaction save(WebhookTransaction transaction) {
        return springDataRepo.save(transaction);
    }

    @Override
    public Optional<WebhookTransaction> findByMessageId(String messageId) {
        return springDataRepo.findByMessageId(messageId);
    }

    @Override
    public boolean existsByMessageId(String messageId) {
        return springDataRepo.existsByMessageId(messageId);
    }
}

interface SpringDataWebhookTransactionRepo extends JpaRepository<WebhookTransaction, UUID> {
    Optional<WebhookTransaction> findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
}
