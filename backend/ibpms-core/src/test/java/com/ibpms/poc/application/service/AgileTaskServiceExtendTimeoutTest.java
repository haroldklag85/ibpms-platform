package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.enums.ClaimActionType;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AgileTaskService.extendTimeout() (CA-19/CA-20).
 */
class AgileTaskServiceExtendTimeoutTest {

    private AgileTaskRepositoryJpa taskRepository;
    private ClaimAuditService claimAuditService;
    private WorkdeskNotificationService notificationService;
    private AgileTaskService taskService;

    @BeforeEach
    void setup() {
        taskRepository = mock(AgileTaskRepositoryJpa.class);
        claimAuditService = mock(ClaimAuditService.class);
        notificationService = mock(WorkdeskNotificationService.class);

        taskService = new AgileTaskService(
                taskRepository,
                mock(SlaChangeLogService.class),
                mock(AuditLogService.class),
                mock(SimpMessagingTemplate.class),
                mock(com.ibpms.poc.infrastructure.jpa.repository.TaskAuditLogRepository.class),
                mock(FormFieldCleanserService.class),
                claimAuditService,
                notificationService
        );
    }

    @Test
    @DisplayName("CA-19: Debe rechazar extensión cuando se alcanza el límite de 2")
    void shouldLimitExtensionsToMaxTwo() {
        AgileTask task = buildClaimedTask("user1", 2);
        when(taskRepository.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.extendTimeout(task.getId(), "user1"));

        assertTrue(ex.getReason().contains("límite de 2"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("CA-19: Debe actualizar lastActivityAt al extender timeout")
    void shouldResetLastActivityOnExtend() {
        AgileTask task = buildClaimedTask("user1", 0);
        ZonedDateTime before = task.getLastActivityAt();
        when(taskRepository.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));

        taskService.extendTimeout(task.getId(), "user1");

        assertTrue(task.getLastActivityAt().isAfter(before) || task.getLastActivityAt().isEqual(before));
        assertEquals(1, task.getTimeoutExtensions());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("CA-20: Debe registrar TIMEOUT_EXTENDED con mensaje de extensión N/2")
    void shouldRecordEnrichedAuditOnExtend() {
        AgileTask task = buildClaimedTask("user1", 1);
        when(taskRepository.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));

        taskService.extendTimeout(task.getId(), "user1");

        verify(claimAuditService).audit(
                eq(task.getId()),
                eq("user1"),
                eq(ClaimActionType.TIMEOUT_EXTENDED.name()),
                contains("Extensión 2 de 2"),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("CA-19: Debe notificar al supervisor vía WebSocket en cada extensión")
    void shouldNotifyOnTimeoutExtension() {
        AgileTask task = buildClaimedTask("user1", 0);
        task.setTeamId("team-alpha");
        when(taskRepository.findByIdForUpdate(task.getId())).thenReturn(Optional.of(task));

        taskService.extendTimeout(task.getId(), "user1");

        verify(notificationService).notifyTimeoutExtended(
                eq("team-alpha"),
                eq(task.getId().toString()),
                eq("user1"),
                eq(1)
        );
    }

    private AgileTask buildClaimedTask(String assignee, int currentExtensions) {
        AgileTask task = new AgileTask();
        task.setId(UUID.randomUUID());
        task.setStatus("CLAIMED");
        task.setTeamId("default-team");
        task.setAssigneeIds(new HashSet<>(Collections.singletonList(assignee)));
        task.setLastActivityAt(ZonedDateTime.now().minusHours(1));
        task.setTimeoutExtensions(currentExtensions);
        return task;
    }
}
