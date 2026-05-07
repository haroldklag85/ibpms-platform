package com.ibpms.poc.domain.model.kanban;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeLogEntryTest {

    @Test
    void testConstructorInitializesCorrectly() {
        TimeLogEntry entry = new TimeLogEntry(
                UUID.randomUUID(), UUID.randomUUID(), "TASK_AGILE", null, null, null, "user123", null
        );
        assertNotNull(entry.getStartedAt());
        assertNotNull(entry.getCreatedAt());
        assertEquals("user123", entry.getUserId());
    }

    @Test
    void testStopCalculatesDuration() {
        ZonedDateTime start = ZonedDateTime.now().minusMinutes(45);
        TimeLogEntry entry = new TimeLogEntry(
                UUID.randomUUID(), UUID.randomUUID(), "TASK_AGILE", start, null, null, "user123", null
        );
        
        entry.stop(ZonedDateTime.now());
        
        assertNotNull(entry.getStoppedAt());
        assertEquals(45, entry.getDurationMinutes());
    }

    @Test
    void testStopTwiceThrowsException() {
        ZonedDateTime start = ZonedDateTime.now().minusMinutes(45);
        TimeLogEntry entry = new TimeLogEntry(
                UUID.randomUUID(), UUID.randomUUID(), "TASK_AGILE", start, null, null, "user123", null
        );
        
        entry.stop(ZonedDateTime.now());
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> entry.stop(ZonedDateTime.now()));
        assertEquals("El timer ya se encuentra detenido.", exception.getMessage());
    }
}
