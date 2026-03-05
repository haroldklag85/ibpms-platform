package com.ibpms.poc.application.service.mailbox;

import com.ibpms.poc.infrastructure.jpa.entity.SacMailboxEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SacMailboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailboxPollingJob {

    private final SacMailboxRepository mailboxRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * Polling job running every 5 minutes asynchronously to fetch inbound emails.
     * Uses Redis DistributedLock to prevent clustering double-read scenarios.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scanInboundMailboxes() {
        List<SacMailboxEntity> activeMailboxes = mailboxRepository.findByActiveTrue();

        for (SacMailboxEntity mailbox : activeMailboxes) {
            String lockKey = "LOCK:MAILBOX:SCAN:" + mailbox.getId();

            // Distributed Lock implementation via Redis SETNX
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofMinutes(4));

            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    log.info("Lock acquired for Mailbox {}. Scanning Graph API for new emails...", mailbox.getAlias());

                    // 1. Fetch EMAILS via Graph API (Mocked logic for Epic 13)
                    // 2. Feed to Agent 3 (AI Triage)

                    // Requirement CA-3: RAG Fallback simulation
                    simulateAiTriagingWithFallback(mailbox);

                } catch (Exception e) {
                    log.error("Error processing mailbox {}: {}", mailbox.getAlias(), e.getMessage());
                } finally {
                    // Release the Redis Lock
                    redisTemplate.delete(lockKey);
                    log.info("Released Distributed Lock for Mailbox {}", mailbox.getAlias());
                }
            } else {
                log.debug("Mailbox {} is currently being processed by another cluster node. Skipping.",
                        mailbox.getAlias());
            }
        }
    }

    private void simulateAiTriagingWithFallback(SacMailboxEntity mailbox) {
        // Simulating Agent 3 processing (RAG classification)
        double aiConfidenceScore = Math.random();

        if (aiConfidenceScore < 0.20) {
            // IA fails to classify or confidence is too low -> FALLBACK
            log.warn("AI Confidence Score ({}%) is too low. Executing RAG Fallback to default ProcessId: {}",
                    Math.round(aiConfidenceScore * 100), mailbox.getDefaultBpmnProcessId());

            // Logic to instruct Camunda to start the 'defaultBpmnProcessId' bypassing AI.
        } else {
            log.info("AI Confidence Score ({}%) is acceptable. Routing dynamically.",
                    Math.round(aiConfidenceScore * 100));
        }
    }
}
