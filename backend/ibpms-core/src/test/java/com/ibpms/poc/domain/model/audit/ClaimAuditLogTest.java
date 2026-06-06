package com.ibpms.poc.domain.model.audit;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ClaimAuditLogTest {

    @Test
    public void testPojoPuro() {
        UUID taskId = UUID.randomUUID();
        Instant now = Instant.now();
        
        ClaimAuditLog log = new ClaimAuditLog(
                taskId,
                "user1",
                "CLAIMED",
                "tenantA",
                now,
                "user0",
                "Reassigned",
                "Taking over"
        );
        
        assertEquals(taskId, log.getTaskId());
        assertEquals("user1", log.getUserId());
        assertEquals("CLAIMED", log.getActionType());
        assertEquals("tenantA", log.getTenantId());
        assertEquals(now, log.getTimestamp());
        assertEquals("user0", log.getPreviousAssignee());
        assertEquals("Reassigned", log.getReason());
        assertEquals("Taking over", log.getMessage());
        
        // Verificar que no hay anotaciones JPA en la clase (reflectivamente)
        java.lang.annotation.Annotation[] annotations = ClaimAuditLog.class.getAnnotations();
        boolean hasEntity = false;
        for (java.lang.annotation.Annotation annotation : annotations) {
            if (annotation.annotationType().getName().contains("Entity")) {
                hasEntity = true;
            }
        }
        assertFalse(hasEntity, "ClaimAuditLog debe ser un POJO puro sin @Entity");
    }
}
