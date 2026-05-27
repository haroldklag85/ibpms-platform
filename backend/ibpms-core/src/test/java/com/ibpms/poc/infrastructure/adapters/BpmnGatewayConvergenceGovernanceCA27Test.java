package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Test for Technical Debt CA-27 (Validaciones Topológicas Avanzadas - Convergencia).
 * This test is expected to fail (Red Phase) until the logic is implemented to
 * detect and reject divergent parallel/inclusive gateways that never converge.
 * 
 * // @Traceability: US-005, CA-27 Validaciones Topológicas Avanzadas (Convergencia)
 */
public class BpmnGatewayConvergenceGovernanceCA27Test {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void testDivergentParallelGatewayWithoutConvergenceShouldReturnError() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Definitions_1\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:parallelGateway id=\"Gateway_1\">\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_2</bpmn:outgoing>\n" +
                "      <bpmn:outgoing>Flow_3</bpmn:outgoing>\n" +
                "    </bpmn:parallelGateway>\n" +
                "    <bpmn:task id=\"Task_A\" name=\"Task A\">\n" +
                "      <bpmn:incoming>Flow_2</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_4</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:task id=\"Task_B\" name=\"Task B\">\n" +
                "      <bpmn:incoming>Flow_3</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_5</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\">\n" +
                "      <bpmn:incoming>Flow_4</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:endEvent id=\"EndEvent_2\">\n" +
                "      <bpmn:incoming>Flow_5</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"Gateway_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"Gateway_1\" targetRef=\"Task_A\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_3\" sourceRef=\"Gateway_1\" targetRef=\"Task_B\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_4\" sourceRef=\"Task_A\" targetRef=\"EndEvent_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_5\" sourceRef=\"Task_B\" targetRef=\"EndEvent_2\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        assertFalse(result.isPassed(), "A divergent gateway without convergence must return an ERROR, so isPassed() should be false.");
        
        boolean hasConvergenceError = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) &&
                        (issue.getRule().toLowerCase().contains("convergenc") || 
                         issue.getMessage().toLowerCase().contains("convergenc")));
                         
        assertTrue(hasConvergenceError, "There should be an ERROR issue mentioning 'convergence' or 'convergencia'.");
    }
}
