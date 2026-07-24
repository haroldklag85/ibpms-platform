// @Traceability: US-003 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.model;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
 * Immutable transaction record for each incoming webhook (US-004 CA-1).
 * The unique constraint on message_id enforces idempotency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookTransaction {
    private UUID id;
    private String messageId;
    private String senderEmail;
    private String senderDomain;
    private String subject;
    private String payloadHash;
    private String status;
    private String rejectionReason;
    private String camundaProcessInstanceId;
    private ZonedDateTime createdAt;
}
