package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba que evidencia la deuda técnica (bucle infinito).
 * // @Traceability: US-005, CA-23 Detección de Bucles Topológicos
 */
public class BpmnInfiniteLoopGovernanceCA23Test {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void shouldFailWhenSynchronousInfiniteLoopIsPresent() {
        // XML BPMN con ciclo síncrono: StartEvent -> Task -> Gateway -> Task
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"start\">\n" +
                "      <bpmn:outgoing>flow1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:serviceTask id=\"task\" name=\"Sync Task\" camunda:delegateExpression=\"${myDelegate}\">\n" +
                "      <bpmn:incoming>flow1</bpmn:incoming>\n" +
                "      <bpmn:incoming>flow3</bpmn:incoming>\n" +
                "      <bpmn:outgoing>flow2</bpmn:outgoing>\n" +
                "    </bpmn:serviceTask>\n" +
                "    <bpmn:exclusiveGateway id=\"gw\" name=\"Gateway\" default=\"flow3\">\n" +
                "      <bpmn:incoming>flow2</bpmn:incoming>\n" +
                "      <bpmn:outgoing>flow3</bpmn:outgoing>\n" +
                "      <bpmn:outgoing>flow4</bpmn:outgoing>\n" +
                "    </bpmn:exclusiveGateway>\n" +
                "    <bpmn:endEvent id=\"end\">\n" +
                "      <bpmn:incoming>flow4</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"flow1\" sourceRef=\"start\" targetRef=\"task\" />\n" +
                "    <bpmn:sequenceFlow id=\"flow2\" sourceRef=\"task\" targetRef=\"gw\" />\n" +
                "    <bpmn:sequenceFlow id=\"flow3\" sourceRef=\"gw\" targetRef=\"task\" />\n" +
                "    <bpmn:sequenceFlow id=\"flow4\" sourceRef=\"gw\" targetRef=\"end\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Ejecutar validación
        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        // Actualmente el método no detecta bucles, por lo tanto el assert de falsedad fallará en rojo.
        // Se espera que la prueba falle hasta que la deuda técnica se corrija.
        assertFalse(result.isPassed(), "El resultado debe arrojar un error al detectar un bucle topológico infinito");

        boolean hasLoopIssue = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) &&
                        (issue.getRule().contains("LOOP") ||
                         issue.getMessage().toLowerCase().contains("bucle") ||
                         issue.getMessage().toLowerCase().contains("ciclo")));

        assertTrue(hasLoopIssue, "Debe existir un issue de tipo ERROR asociado a ciclos o infinite loops");
    }
}
