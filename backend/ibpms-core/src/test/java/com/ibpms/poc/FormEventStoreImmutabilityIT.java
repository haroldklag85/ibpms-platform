package com.ibpms.poc;

import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class FormEventStoreImmutabilityIT extends AbstractIntegrationIT {

    @Autowired
    private FormEventRepository formEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFormEventStoreIsAppendOnly() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        FormEvent event = FormEvent.builder()
                .eventId(eventId)
                .eventType(EventType.FORM_SUBMITTED)
                .taskId("task-123")
                .processInstanceId("proc-456")
                .userId("user1")
                .payloadJson("{\"field\":\"value\"}")
                .schemaVersion("V1")
                .createdAt(ZonedDateTime.now())
                .build();

        // Act - Insert allows it (Append)
        FormEvent saved = formEventRepository.save(event);
        assertNotNull(saved);
        
        // Assert Immutability for UPDATE
        RuntimeException updateException = assertThrows(RuntimeException.class, () -> {
            jdbcTemplate.update("UPDATE form_event_store SET payload_json = '{\"hacked\": true}' WHERE event_id = ?", eventId);
        });
        assertTrue(updateException.getMessage().contains("FORBIDDEN: Event Store is append-only"), "Should trigger immutability exception");

        // Assert Immutability for DELETE
        RuntimeException deleteException = assertThrows(RuntimeException.class, () -> {
            jdbcTemplate.update("DELETE FROM form_event_store WHERE event_id = ?", eventId);
        });
        assertTrue(deleteException.getMessage().contains("FORBIDDEN: Event Store is append-only"), "Should trigger immutability exception");
    }
}
