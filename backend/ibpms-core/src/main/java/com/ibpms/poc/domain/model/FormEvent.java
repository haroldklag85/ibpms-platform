package com.ibpms.poc.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.UUID;

// SOLO Lombok permitido. Cero imports jakarta.*, hibernate.*​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
@Value   // inmutable por diseño CQRS
@Builder
public class FormEvent {
    UUID eventId;
    EventType eventType;
    String taskId;
    String processInstanceId;
    String userId;
    String payloadJson;
    String schemaVersion;
    UUID idempotencyKey;
    UUID originalEventId;
    ZonedDateTime createdAt;
}
