package com.ibpms.poc.application.service;

import com.ibpms.poc.application.config.ClaimProperties;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.enums.ClaimActionType;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GhostJobScheduler (CA-15: Ghost timeout per-tenant).
 */
class GhostJobSchedulerTest {

    private AgileTaskRepositoryJpa taskRepository;
    private ClaimAuditService claimAuditService;
    private SimpMessagingTemplate messagingTemplate;
    private ClaimProperties claimProperties;
    private GhostJobScheduler scheduler;

    @BeforeEach
    void setup() {
        taskRepository = mock(AgileTaskRepositoryJpa.class);
        claimAuditService = mock(ClaimAuditService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        claimProperties = mock(ClaimProperties.class);

        when(claimProperties.getGhostTimeout()).thenReturn(240);
        when(claimProperties.getTimeoutForTenant("default")).thenReturn(240);

        scheduler = new GhostJobScheduler(taskRepository, claimAuditService, messagingTemplate, claimProperties);
    }

    @Test
    @DisplayName("CA-15: Debe auto-unclaim después de timeout específico de tenant")
    void shouldAutoUnclaimAfterTenantSpecificTimeout() {
        // Tenant "tenantA" tiene override de 120 min
        when(claimProperties.getTimeoutForTenant("tenantA")).thenReturn(120);

        AgileTask ghostTask = buildClaimedTask("user1", "tenantA", ZonedDateTime.now().minusMinutes(130));
        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(ghostTask));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("AVAILABLE", ghostTask.getStatus());
        assertEquals(0, ghostTask.getTimeoutExtensions());
        verify(taskRepository).save(ghostTask);
        verify(claimAuditService).audit(eq(ghostTask.getId()), eq("system"),
                eq(ClaimActionType.AUTO_UNCLAIMED.name()), anyString(), eq("user1"), isNull());
    }

    @Test
    @DisplayName("CA-15: Debe usar default timeout (240 min) cuando no hay override de tenant")
    void shouldUseDefaultTimeoutWhenNoTenantOverride() {
        when(claimProperties.getTimeoutForTenant("default")).thenReturn(240);

        // Tarea con 200 min — NO debe expirar con default de 240
        AgileTask task = buildClaimedTask("user2", null, ZonedDateTime.now().minusMinutes(200));
        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(task));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("CLAIMED", task.getStatus());
        verify(taskRepository, never()).save(task);
        verify(claimAuditService, never()).audit(any(), any(), eq(ClaimActionType.AUTO_UNCLAIMED.name()), any(), any(), any());
    }

    @Test
    @DisplayName("CA-15: Debe emitir GHOST_WARNING al 75% del umbral")
    void shouldEmitGhostWarningAt75Percent() {
        when(claimProperties.getTimeoutForTenant("default")).thenReturn(240);

        // 200 min > 75% de 240 (180), pero < 240 → warning
        AgileTask warnTask = buildClaimedTask("user3", null, ZonedDateTime.now().minusMinutes(200));
        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(warnTask));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("CLAIMED", warnTask.getStatus());
        verify(messagingTemplate).convertAndSend(eq("/topic/tasks/user/user3"), any(Map.class));
    }

    @Test
    @DisplayName("CA-15: NO debe auto-unclaim antes del threshold")
    void shouldNotAutoUnclaimBeforeThreshold() {
        when(claimProperties.getTimeoutForTenant("default")).thenReturn(240);

        // 100 min < 180 (75% de 240) → ni warning ni unclaim
        AgileTask safeTask = buildClaimedTask("user4", null, ZonedDateTime.now().minusMinutes(100));
        when(taskRepository.findByStatus("CLAIMED")).thenReturn(Collections.singletonList(safeTask));

        scheduler.detectAndProcessGhostTasks();

        assertEquals("CLAIMED", safeTask.getStatus());
        verify(taskRepository, never()).save(safeTask);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/tasks/user/user4"), any(Map.class));
    }

    private AgileTask buildClaimedTask(String assignee, String tenantId, ZonedDateTime lastActivity) {
        AgileTask task = new AgileTask();
        task.setId(UUID.randomUUID());
        task.setStatus("CLAIMED");
        task.setTenantId(tenantId);
        task.setAssigneeIds(new HashSet<>(Collections.singletonList(assignee)));
        task.setLastActivityAt(lastActivity);
        task.setTimeoutExtensions(0);
        return task;
    }
}

