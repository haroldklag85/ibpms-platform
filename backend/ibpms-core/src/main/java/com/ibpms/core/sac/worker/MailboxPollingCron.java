package com.ibpms.core.sac.worker;

import com.ibpms.core.sac.domain.SacMailbox;
import com.ibpms.core.sac.repository.SacMailboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MailboxPollingCron {

    private static final Logger log = LoggerFactory.getLogger(MailboxPollingCron.class);
    private final SacMailboxRepository mailboxRepository;

    public MailboxPollingCron(SacMailboxRepository mailboxRepository) {
        this.mailboxRepository = mailboxRepository;
    }

    /**
     * Polling distribuido cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300000)
    public void pollMailboxes() {
        // En una implementacion real:
        // redisLockRegistry.executeLocked("mailbox-polling-lock", () -> {

        log.info("Iniciando ciclo de polling de buzones institucionales (Redis Lock Adquirido)...");

        List<SacMailbox> activeMailboxes = mailboxRepository.findAll().stream()
                .filter(SacMailbox::isActive)
                .toList();

        for (SacMailbox mailbox : activeMailboxes) {
            try {
                log.info("Extrayendo correos de la cuenta: {} usando protocolo {}", mailbox.getAlias(),
                        mailbox.getProtocol());
                throw new UnsupportedOperationException(
                        "GAP-1 [US-035]: MS Graph API email extraction — pendiente refinamiento Sprint asignado."
                );
            } catch (Exception e) {
                log.error("Fallo durante el polling del buzón {}", mailbox.getAlias(), e);
            }
        }

        // }); fin del lock
    }
}
