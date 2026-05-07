package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BulkClaimServiceTest {

    private AgileTaskRepositoryJpa taskRepository;
    private ClaimAuditService claimAuditService;
    private SimpMessagingTemplate messagingTemplate;
    private AgileTaskService service;

    @BeforeEach
    public void setup() {
        taskRepository = mock(AgileTaskRepositoryJpa.class);
        claimAuditService = mock(ClaimAuditService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        
        service = new AgileTaskService(taskRepository, mock(SlaChangeLogService.class), mock(AuditLogService.class), 
                messagingTemplate, mock(com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository.class), 
                mock(FormFieldCleanserService.class), claimAuditService);
    }

    @Test
    public void testBulkClaimOk() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        AgileTask task1 = new AgileTask(); task1.setId(id1); task1.setStatus("AVAILABLE");
        AgileTask task2 = new AgileTask(); task2.setId(id2); task2.setStatus("OPEN");
        
        when(taskRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(task1));
        when(taskRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(task2));

        Map<String, Object> result = service.bulkClaim(Arrays.asList(id1.toString(), id2.toString()), "userX");

        List<String> claimed = (List<String>) result.get("claimed");
        assertEquals(2, claimed.size());
        verify(claimAuditService, times(2)).audit(any(), eq("userX"), eq("BULK_CLAIMED"), any(), any(), any());
        verify(messagingTemplate).convertAndSend(eq("/topic/tasks"), any(Map.class));
    }

    @Test
    public void testBulkClaimPartial() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        AgileTask task1 = new AgileTask(); task1.setId(id1); task1.setStatus("AVAILABLE");
        AgileTask task2 = new AgileTask(); task2.setId(id2); task2.setStatus("CLAIMED"); // Conflicto
        
        when(taskRepository.findByIdForUpdate(id1)).thenReturn(Optional.of(task1));
        when(taskRepository.findByIdForUpdate(id2)).thenReturn(Optional.of(task2));

        Map<String, Object> result = service.bulkClaim(Arrays.asList(id1.toString(), id2.toString()), "userX");

        List<String> claimed = (List<String>) result.get("claimed");
        List<Map<String, String>> conflicts = (List<Map<String, String>>) result.get("conflicts");
        
        assertEquals(1, claimed.size());
        assertEquals(1, conflicts.size());
        assertEquals("Task is not AVAILABLE", conflicts.get(0).get("reason"));
    }
}
