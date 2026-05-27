package com.ibpms.poc.infrastructure.adapters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * TDD Test for Technical Debt CA-09 (Strict Deployment Governance).
 * This test is expected to fail (Red Phase) until the logic is implemented to
 * return an ERROR instead of a WARNING for specific structural BPMN deficits.
 * 
 * // @Traceability: US-005, CA-09 Gobernanza Estricta de Despliegue
 */
public class BpmnStructuralGovernanceCA09Test {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void testCallActivityMissingKeyShouldReturnError() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:callActivity id=\"CallActivity_1\" name=\"Subproceso\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Execute validation with strictness level (e.g., 100)
        var result = adapter.validateDraftXml(xml, 100);

        // In TDD, this should initially fail (Red Phase) because the adapter 
        // incorrectly throws a WARNING instead of an ERROR.
        assertFalse(result.isPassed(), "A CallActivity without calledElement must return an ERROR, so isPassed() should be false.");
    }

    @Test
    void testMessageEventMissingRefShouldReturnError() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:intermediateCatchEvent id=\"Event_1\">\n" +
                "      <bpmn:messageEventDefinition id=\"Message_1\" />\n" +
                "    </bpmn:intermediateCatchEvent>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Execute validation with strictness level (e.g., 100)
        var result = adapter.validateDraftXml(xml, 100);

        // In TDD, this should initially fail (Red Phase) because the adapter 
        // incorrectly throws a WARNING instead of an ERROR.
        assertFalse(result.isPassed(), "A MessageEvent without messageRef must return an ERROR, so isPassed() should be false.");
    }
}
