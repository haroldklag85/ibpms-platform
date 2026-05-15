package com.ibpms.poc.domain.model;

import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FormEventTest {

    @Test
    void testFormEventImmutabilityAndBuilder() {
        UUID eventId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        FormEvent event = FormEvent.builder()
                .eventId(eventId)
                .eventType(EventType.FORM_SUBMITTED)
                .taskId("task-123")
                .processInstanceId("proc-123")
                .userId("user-abc")
                .payloadJson("{\"key\":\"value\"}")
                .schemaVersion("1.0")
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .build();

        assertEquals(eventId, event.getEventId());
        assertEquals(EventType.FORM_SUBMITTED, event.getEventType());
        assertEquals("task-123", event.getTaskId());
        assertEquals("proc-123", event.getProcessInstanceId());
        assertEquals("user-abc", event.getUserId());
        assertEquals("{\"key\":\"value\"}", event.getPayloadJson());
        assertEquals("1.0", event.getSchemaVersion());
        assertEquals(idempotencyKey, event.getIdempotencyKey());
        assertEquals(now, event.getCreatedAt());
        assertNull(event.getOriginalEventId());

        // Validate @Value generates equals correctly
        FormEvent event2 = FormEvent.builder()
                .eventId(eventId)
                .eventType(EventType.FORM_SUBMITTED)
                .taskId("task-123")
                .processInstanceId("proc-123")
                .userId("user-abc")
                .payloadJson("{\"key\":\"value\"}")
                .schemaVersion("1.0")
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .build();

        assertEquals(event, event2);
    }
}
