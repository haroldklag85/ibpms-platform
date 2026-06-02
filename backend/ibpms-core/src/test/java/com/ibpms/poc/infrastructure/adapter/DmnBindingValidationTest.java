// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DmnBindingValidationTest {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    @DisplayName("TDD CA-12: Debe fallar (marcar error) si un BusinessRuleTask no tiene camunda:decisionRefBinding")
    void shouldFailWhenDecisionRefBindingIsMissing() {
        // Arrange
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\"\n" +
                "                  targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "        <camunda:properties>\n" +
                "            <camunda:property name=\"ReglaNomenclatura\" value=\"Test\" />\n" +
                "        </camunda:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"test_form\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:businessRuleTask id=\"BusinessRuleTask_1\" name=\"Decide\" camunda:decisionRef=\"my_dmn\">\n" +
                "      <!-- No decisionRefBinding specified -->\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_2</bpmn:outgoing>\n" +
                "    </bpmn:businessRuleTask>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\">\n" +
                "      <bpmn:incoming>Flow_2</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"BusinessRuleTask_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"BusinessRuleTask_1\" targetRef=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        List<String> activeTopics = Collections.emptyList();
        List<String> vipRoles = Collections.emptyList();

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        // Assert
        // El test FALLARÁ inicialmente porque el adapter actualmente solo agrega un WARNING
        // y deja response.isValid() = true. Nosotros esperamos que sea un error (Fase ROJA).
        assertFalse(response.isValid(), "El resultado de la validación debería ser inválido por CA-12");
        
        boolean hasCa12Error = response.getErrors().stream()
            .anyMatch(err -> err.getMessage().toLowerCase().contains("decisionrefbinding"));
        
        assertTrue(hasCa12Error, "Debe contener un error sobre la falta de decisionRefBinding (CA-12)");
    }

    @Test
    @DisplayName("TDD CA-12: Debe fallar (marcar error) si un BusinessRuleTask usa camunda:decisionRefBinding='latest'")
    void shouldFailWhenDecisionRefBindingIsLatest() {
        // Arrange
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\"\n" +
                "                  targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "        <camunda:properties>\n" +
                "            <camunda:property name=\"ReglaNomenclatura\" value=\"Test\" />\n" +
                "        </camunda:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"test_form\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:businessRuleTask id=\"BusinessRuleTask_1\" name=\"Decide\" camunda:decisionRef=\"my_dmn\" camunda:decisionRefBinding=\"latest\">\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_2</bpmn:outgoing>\n" +
                "    </bpmn:businessRuleTask>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\">\n" +
                "      <bpmn:incoming>Flow_2</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"BusinessRuleTask_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"BusinessRuleTask_1\" targetRef=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        List<String> activeTopics = Collections.emptyList();
        List<String> vipRoles = Collections.emptyList();

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        // Assert
        assertFalse(response.isValid(), "El resultado de la validación debería ser inválido por CA-12");
        
        boolean hasCa12Error = response.getErrors().stream()
            .anyMatch(err -> err.getMessage().toLowerCase().contains("latest") || err.getMessage().toLowerCase().contains("decisionrefbinding"));
        
        assertTrue(hasCa12Error, "Debe contener un error sobre el uso de decisionRefBinding='latest' (CA-12)");
    }
}
