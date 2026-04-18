package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RejectionLogServiceTest {

    @Test
    void getRejectionHistory_returnsFormattedListForBff() {
        // Arrange
        FormEventRepository repositoryMock = mock(FormEventRepository.class);
        RejectionLogService service = new RejectionLogService(repositoryMock);

        String processInstanceId = "PROC-123";
        FormEvent mockEvent = mock(FormEvent.class);
        when(mockEvent.getEventId()).thenReturn(UUID.randomUUID());
        when(mockEvent.getCreatedAt()).thenReturn(ZonedDateTime.now());
        when(mockEvent.getUserId()).thenReturn("user-1");
        when(mockEvent.getTaskId()).thenReturn("TASK-1");

        // By reflection or setters assume eventId and createdAt are set. 
        // For standard entity logic they are generated on init or DB save.

        when(repositoryMock.findByProcessInstanceIdAndEventType(processInstanceId, EventType.FORM_REJECTED))
                .thenReturn(List.of(mockEvent));

        // Act
        List<Map<String, Object>> history = service.getRejectionHistory(processInstanceId);

        // Assert
        assertThat(history).hasSize(1);
        Map<String, Object> historyEntry = history.get(0);
        assertThat(historyEntry.get("taskId")).isEqualTo("TASK-1");
        assertThat(historyEntry.get("userId")).isEqualTo("user-1");
        assertThat(historyEntry.get("reason")).isEqualTo("Rechazado en etapa anterior (Validación Automática)");
    }
}
