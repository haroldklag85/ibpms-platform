package com.ibpms.poc.application.service;

import com.ibpms.poc.application.config.ClaimProperties;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GhostJobSchedulerTest {

    private AgileTaskRepositoryJpa taskRepository;
    private ClaimAuditService claimAuditService;
    private SimpMessagingTemplate messagingTemplate;
    private ClaimProperties claimProperties;
    private GhostJobScheduler scheduler;

    @BeforeEach
    public void setup() {
        taskRepository = mock(AgileTaskRepositoryJpa.class);
        claimAuditService = mock(ClaimAuditService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        claimProperties = mock(ClaimProperties.class);
        
        when(claimProperties.getGhostTimeout()).thenReturn(240); // 4 horas
        scheduler = new GhostJobScheduler(taskRepository, claimAuditService, messagingTemplate, claimProperties);
    }

    @Test
    public void testDetectGhostTask() {
        AgileTask ghostTask = new AgileTask();
        ghostTask.setId(UUID.randomUUID());
        ghostTask.setStatus("CLAIMED");
        ghostTask.setAssigneeIds(new HashSet<>(Collections.singletonList("user1")));
        ghostTask.setLastActivityAt(ZonedDateTime.now().minusHours(5)); // Expirada

        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(ghostTask));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("AVAILABLE", ghostTask.getStatus());
        verify(taskRepository).save(ghostTask);
        verify(claimAuditService).audit(eq(ghostTask.getId()), eq("system"), eq("AUTO_UNCLAIMED"), anyString(), eq("user1"), isNull());
        
        // Verifica webhooks emitidos
        verify(messagingTemplate).convertAndSend(eq("/topic/tasks/user/user1"), any(Map.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/tasks"), any(Map.class));
    }

    @Test
    public void testPreWarningGhostTask() {
        AgileTask warnTask = new AgileTask();
        warnTask.setId(UUID.randomUUID());
        warnTask.setStatus("CLAIMED");
        warnTask.setAssigneeIds(new HashSet<>(Collections.singletonList("user2")));
        warnTask.setLastActivityAt(ZonedDateTime.now().minusMinutes(200)); // 200 > 75% de 240 (180)

        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(warnTask));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("CLAIMED", warnTask.getStatus()); // No debe cambiar aún
        verify(taskRepository, never()).save(warnTask);
        verify(claimAuditService, never()).audit(any(), any(), any(), any(), any(), any());
        
        // Debe emitir warning
        verify(messagingTemplate).convertAndSend(eq("/topic/tasks/user/user2"), any(Map.class));
    }
}
