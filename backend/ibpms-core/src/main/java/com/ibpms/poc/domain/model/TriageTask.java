// @Traceability: US-003 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.model;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriageTask {
    private UUID id;
    private String camundaProcessInstanceId;
    private String messageId;
    private String senderEmail;
    private String subject;
    private Integer attachmentCount;
    private String status;
    private String rejectionReason;
    private ZonedDateTime slaDeadline;
    private String scanStatus;
    private String fileSha256Hash;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
