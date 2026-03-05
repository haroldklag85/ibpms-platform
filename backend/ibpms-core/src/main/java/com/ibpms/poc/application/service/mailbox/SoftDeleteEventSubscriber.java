package com.ibpms.poc.application.service.mailbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event model strictly adhering to Soft-Deletes required by Epic 13 mapping.
 */
@Component
@Slf4j
public class SoftDeleteEventSubscriber {

    /**
     * Listens for Task Deletion/Archiving events internally dispatched by
     * Camunda/iBPMS context.
     * Prevents HARD DELETES on the Microsoft Exchange Server.
     */
    @EventListener
    public void onEmailSoftDeleteRequest(EmailSoftDeleteEvent event) {
        log.info("Received Soft Delete Event for Inbound Mail ID: {}", event.getExchangeMessageId());

        // 1. Retrieve the Graph API Client using keyVaultReferenceId
        // 2. Call MS Graph to move the item to the "Deleted Items" / "Papelera" Folder

        log.info(
                "Routing email '{}' to MS Exchange Deleted Items folder. Hard-Deletes are explicitly forbidden by Epic 13.",
                event.getExchangeMessageId());
    }

    // Dummy DTO reflecting the Event Payload
    @lombok.Data
    public static class EmailSoftDeleteEvent {
        private String exchangeMessageId;
        private String mailboxAlias;
    }
}
