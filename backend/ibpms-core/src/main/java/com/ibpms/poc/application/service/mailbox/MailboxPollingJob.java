package com.ibpms.poc.application.service.mailbox;

import com.ibpms.core.sac.domain.SacMailbox;
import com.ibpms.core.sac.repository.SacMailboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * FENCED — Sprint 4 Profilaxis (US-011 Docketing SAC).
 * Este componente es un duplicado funcional de MailboxPollingCron (com.ibpms.core.sac.worker).
 * Desactivado como @Component para evitar cruce de fronteras de paquete con com.ibpms.core.sac.
 * Se reactivará cuando US-011 entre en construcción funcional.
 */
// @Component — FENCED: S4 Profilaxis — Duplicado de MailboxPollingCron
@RequiredArgsConstructor
@Slf4j
public class MailboxPollingJob {

    private final SacMailboxRepository mailboxRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * Polling job running every 5 minutes asynchronously to fetch inbound emails.
     * Uses Redis DistributedLock to prevent clustering double-read scenarios.
     */
    // @Scheduled(fixedRate = 300000) — FENCED: S4 Profilaxis
    public void scanInboundMailboxes() {
        throw new UnsupportedOperationException(
                "FENCED [US-011]: MailboxPollingJob desactivado por profilaxis S4. " +
                "Use MailboxPollingCron (com.ibpms.core.sac.worker) como referencia canónica."
        );
    }

    private void simulateAiTriagingWithFallback(SacMailbox mailbox) {
        throw new UnsupportedOperationException(
                "FENCED [US-011]: AI Triaging pendiente de construcción funcional."
        );
    }
}

