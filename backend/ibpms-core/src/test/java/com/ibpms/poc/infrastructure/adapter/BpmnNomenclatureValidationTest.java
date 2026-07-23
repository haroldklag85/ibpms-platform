// @Traceability: US-005, CA-05 - Nomenclature Validation
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BpmnNomenclatureValidationTest {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    private String createBpmnXml(boolean withNomenclature) {
        String nomenclatureSnippet = withNomenclature 
            ? "      <camunda:property name=\"ReglaNomenclatura\" value=\"CASO-{id}\" />\n" 
            : "";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\"\n" +
                "                  targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\" camunda:versionTag=\"1.0.0\">\n" +
                "    <bpmn:extensionElements>\n" +
                nomenclatureSnippet +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"test_form\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\">\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";
    }

    @Test
    @DisplayName("Debe pasar la validación de borrador cuando se define la Regla de Nomenclatura")
    void shouldPassDraftValidationWhenNomenclatureIsDefined() {
        // Arrange
        String xml = createBpmnXml(true);

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        // Assert
        assertTrue(result.isPassed(), "La validación del borrador debe pasar si tiene nomenclatura definida.");
        boolean hasNomenclatureError = result.getIssues().stream()
                .anyMatch(issue -> "PROCESS_NO_NOMENCLATURE".equals(issue.getRule()));
        assertFalse(hasNomenclatureError, "No debe contener el error de nomenclatura.");
    }

    @Test
    @DisplayName("Debe fallar la validación de borrador cuando no se define la Regla de Nomenclatura")
    void shouldFailDraftValidationWhenNomenclatureIsMissing() {
        // Arrange
        String xml = createBpmnXml(false);

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(xml, 100);

        // Assert
        assertFalse(result.isPassed(), "La validación del borrador debe fallar si falta la nomenclatura.");
        boolean hasNomenclatureError = result.getIssues().stream()
                .anyMatch(issue -> "PROCESS_NO_NOMENCLATURE".equals(issue.getRule()) && "ERROR".equals(issue.getSeverity()));
        assertTrue(hasNomenclatureError, "Debe registrar un error de tipo PROCESS_NO_NOMENCLATURE.");
    }
}
