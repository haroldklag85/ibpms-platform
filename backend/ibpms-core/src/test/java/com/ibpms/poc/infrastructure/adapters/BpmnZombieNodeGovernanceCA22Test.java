package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integración TDD para validar la Detección de Nodos Zombie.
 * Evidencia la deuda técnica CA-22 en CamundaBpmnValidationAdapter.
 * 
 * // @Traceability: US-005, CA-22 Detección de Nodos Zombie
 */
class BpmnZombieNodeGovernanceCA22Test {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    @DisplayName("Debe fallar (Rojo) al detectar un UserTask Zombie (sin incoming ni outgoing)")
    void testZombieNodeValidationFails() {
        // Arrange: Nodo arrojado al lienzo pero sin conectar
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:userTask id=\"Task_Zombie\" name=\"Nodo Zombie\" camunda:formKey=\"sys_generic_form\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        // Assert: El resultado debe ser Passed = false debido al nodo zombie
        assertFalse(result.isPassed(), "La validación debe fallar (isPassed = false) por la presencia de un nodo zombie.");

        // Verificar que existe un issue de tipo ERROR asociado a Nodos Zombie o sin conexiones
        boolean hasZombieError = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) &&
                        (issue.getMessage().toLowerCase().contains("zombie") ||
                         issue.getMessage().toLowerCase().contains("sin conexion") ||
                         issue.getMessage().toLowerCase().contains("sin conexión") ||
                         issue.getMessage().toLowerCase().contains("incoming")));
                         
        assertTrue(hasZombieError, "Debe existir un Issue con nivel ERROR indicando la presencia del nodo zombie o la falta de conexiones.");
    }

    @Test
    @DisplayName("Debe fallar (Rojo) al detectar un nodo colgado (con incoming pero sin outgoing)")
    void testHangingNodeValidationFails() {
        // Arrange: Nodo con entrada (incoming) pero sin salida (outgoing) que NO es un EndEvent
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"sys_start_form\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"Task_Hanging\" />\n" +
                "    <bpmn:userTask id=\"Task_Hanging\" name=\"Nodo Colgado\" camunda:formKey=\"sys_generic_form\">\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "      <!-- Faltaría el <bpmn:outgoing> -->\n" +
                "    </bpmn:userTask>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        // Assert: El resultado debe ser Passed = false debido al nodo colgado
        assertFalse(result.isPassed(), "La validación debe fallar (isPassed = false) por la presencia de un nodo colgado.");

        // Verificar que existe un issue de tipo ERROR asociado al nodo sin salida
        boolean hasHangingError = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) &&
                        (issue.getMessage().toLowerCase().contains("colgado") ||
                         issue.getMessage().toLowerCase().contains("sin salida") ||
                         issue.getMessage().toLowerCase().contains("outgoing") ||
                         issue.getMessage().toLowerCase().contains("sin conexión")));
                         
        assertTrue(hasHangingError, "Debe existir un Issue con nivel ERROR indicando la presencia de un nodo colgado (sin outgoing).");
    }
}
