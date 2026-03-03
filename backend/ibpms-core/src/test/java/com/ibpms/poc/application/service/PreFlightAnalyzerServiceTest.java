package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreFlightAnalyzerServiceTest {

    @Mock
    private BpmnDesignService designService;

    @Mock
    private BpmnDesignAuditLogRepository auditRepository;

    @InjectMocks
    private PreFlightAnalyzerService preFlightService;

    private BpmnProcessDesignEntity mockEntity;
    private final UUID processId = UUID.randomUUID();
    private final String userId = "user123";

    @BeforeEach
    void setUp() {
        mockEntity = new BpmnProcessDesignEntity();
        mockEntity.setId(processId);
        mockEntity.setMaxNodes(100);
        mockEntity.setCurrentVersion(1);
    }

    @Test
    @DisplayName("Debe marcar ERROR cuando un TimerEvent no tiene duración configurada")
    void analizar_TimerSinDuracion_RetornaError() {
        // Arrange
        String xmlBpmMissingTimerDef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\\n" +
                "    <bpmn:intermediateCatchEvent id=\"Timer_1\">\\n" +
                "      <bpmn:timerEventDefinition />\\n" +
                "    </bpmn:intermediateCatchEvent>\\n" +
                "  </bpmn:process>\\n" +
                "</bpmn:definitions>";

        mockEntity.setXmlDraft(xmlBpmMissingTimerDef);
        when(designService.findOrFail(processId)).thenReturn(mockEntity);

        // Act
        PreFlightResultDTO result = preFlightService.analizar(processId, userId);

        // Assert
        assertFalse(result.isPassed());
        boolean hasTimerError = result.getIssues().stream()
                .anyMatch(i -> PreFlightResultDTO.Severity.ERROR.name().equals(i.getSeverity()) &&
                        "TIMER_NO_EXPRESSION".equals(i.getRule()));
        assertTrue(hasTimerError, "Debe detectar el Timer vacío y lanzar ERROR");
    }

    @Test
    @DisplayName("Debe marcar WARNING cuando un MessageEvent no tiene Referencia (messageRef)")
    void analizar_MessageEventSinRef_RetornaWarning() {
        // Arrange
        String xmlBpmMissingMessageRef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\\n" +
                "  <bpmn:process id=\"Process_2\" isExecutable=\"true\">\\n" +
                "    <bpmn:receiveTask id=\"Receive_1\" />\\n" +
                "    <bpmn:intermediateCatchEvent id=\"Msg_1\">\\n" +
                "      <bpmn:messageEventDefinition />\\n" +
                "    </bpmn:intermediateCatchEvent>\\n" +
                "  </bpmn:process>\\n" +
                "</bpmn:definitions>";

        mockEntity.setXmlDraft(xmlBpmMissingMessageRef);
        when(designService.findOrFail(processId)).thenReturn(mockEntity);

        // Act
        PreFlightResultDTO result = preFlightService.analizar(processId, userId);

        // Assert: Pasará si no hay SERVER ERRORs, pero tendrá Warnings
        // En la lógica actual, isPassed() es true si no hay errores
        assertTrue(result.isPassed(), "El proceso pasa porque solo tiene Warnings");
        boolean hasMessageWarning = result.getIssues().stream()
                .anyMatch(i -> PreFlightResultDTO.Severity.WARNING.name().equals(i.getSeverity()) &&
                        "MESSAGE_NO_REF".equals(i.getRule()));
        assertTrue(hasMessageWarning, "Debe detectar el MessageEvent sin referencia como WARNING");
    }
}
