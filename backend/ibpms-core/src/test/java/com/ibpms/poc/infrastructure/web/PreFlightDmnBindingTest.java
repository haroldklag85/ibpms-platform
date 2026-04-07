package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.domain.model.bpmn.DeploymentValidationResponse;
import com.ibpms.poc.domain.service.PreFlightAnalyzerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CA-12: Pre-Flight DMN Binding Validation (Protección Derechos Adquiridos)")
class PreFlightDmnBindingTest {

    @Autowired
    private PreFlightAnalyzerService preFlightAnalyzerService;

    @Test
    @Order(1)
    @DisplayName("CA-12.1: BusinessRuleTask SIN binding genera warning Pre-Flight")
    void testBRT_WithoutBinding_GeneratesWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        // Debe contener al menos un warning referente a CA-12 / binding
        boolean hasBindingWarning = result.getWarnings().stream()
            .anyMatch(w -> w.contains("decisionRefBinding") || w.contains("CA-12") || w.contains("derechos adquiridos"));
        assertTrue(hasBindingWarning,
            "El Pre-Flight debe emitir warning cuando un BRT no tiene binding configurado (CA-12)");
    }

    @Test
    @Order(2)
    @DisplayName("CA-12.2: BusinessRuleTask CON binding='deployment' NO genera warning")
    void testBRT_WithDeploymentBinding_NoWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12_ok" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1"
                    camunda:decisionRefBinding="deployment" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        boolean hasBindingWarning = result.getWarnings().stream()
            .anyMatch(w -> w.contains("BRT_1"));
        assertFalse(hasBindingWarning,
            "El Pre-Flight NO debe emitir warning cuando BRT tiene binding='deployment' (CA-12)");
    }

    @Test
    @Order(3)
    @DisplayName("CA-12.3: BusinessRuleTask CON binding='latest' genera warning informativo")
    void testBRT_WithLatestBinding_GeneratesInfoWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12_latest" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1"
                    camunda:decisionRefBinding="latest" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        // Con el hardening Backend, "latest" ahora emite un warning informativo
        boolean hasLatestInfo = result.getWarnings().stream()
            .anyMatch(w -> w.contains("Late Binding") || w.contains("LATEST"));
        assertTrue(hasLatestInfo,
            "El Pre-Flight debe emitir un warning informativo cuando BRT usa 'latest' (CA-12)");
    }
}
