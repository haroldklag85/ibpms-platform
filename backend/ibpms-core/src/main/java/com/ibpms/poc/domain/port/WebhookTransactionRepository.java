package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.WebhookTransaction;
import java.util.Optional;

/**
 * Hexagonal port for webhook transaction persistence (US-004 CA-1).
 */
public interface WebhookTransactionRepository {
    WebhookTransaction save(WebhookTransaction transaction);
    Optional<WebhookTransaction> findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
}
