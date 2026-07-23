// @Traceability: US-005, CA-15 - Process Version Tag Validation
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.dto.PreFlightResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BpmnVersionTagValidationTest {

    private CamundaBpmnValidationAdapter adapter;
    private List<String> activeTopics;
    private List<String> vipRoles;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
        activeTopics = Collections.emptyList();
        vipRoles = Collections.emptyList();
    }

    private String createBpmnXml(String versionTagAttribute) {
        String versionTagSnippet = versionTagAttribute != null ? " camunda:versionTag=\"" + versionTagAttribute + "\"" : "";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
                "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n" +
                "                  id=\"Definitions_1\"\n" +
                "                  targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\"" + versionTagSnippet + ">\n" +
                "    <bpmn:extensionElements>\n" +
                "        <camunda:property name=\"ReglaNomenclatura\" value=\"Test\" />\n" +
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

    // ── Stream Validation Tests (validateBpmnStream) ─────────────────────────

    @Test
    @DisplayName("shouldPassStreamValidationWhenVersionTagIsValidSemVer")
    void shouldPassStreamValidationWhenVersionTagIsValidSemVer() {
        // Arrange
        String bpmnXml = createBpmnXml("1.0.0");
        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        assertTrue(response.isValid(), "El BPMN con version tag SemVer válido debe ser marcado como válido.");
        long count = response.getErrors().stream().filter(e -> e.getMessage().contains("versionTag")).count();
        assertEquals(0, count, "No debe contener errores de version tag.");
    }

    @Test
    @DisplayName("shouldPassStreamValidationWhenVersionTagIsSemVerWithSuffix")
    void shouldPassStreamValidationWhenVersionTagIsSemVerWithSuffix() {
        // Arrange
        String bpmnXml = createBpmnXml("2.3.1-SNAPSHOT");
        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        // Assert
        assertTrue(response.isValid(), "SemVer con sufijo (por ejemplo, SNAPSHOT) debe ser aceptado.");
    }

    @Test
    @DisplayName("shouldFailStreamValidationWhenVersionTagIsInvalid")
    void shouldFailStreamValidationWhenVersionTagIsInvalid() {
        // Arrange
        String bpmnXml = createBpmnXml("v1.0");
        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        // Assert
        assertFalse(response.isValid(), "El BPMN con version tag no SemVer (v1.0) debe ser rechazado.");
        boolean hasError = response.getErrors().stream()
                .anyMatch(err -> err.getMessage().contains("SemVer") || err.getMessage().contains("versión"));
        assertTrue(hasError, "Debe reportar un error indicando que el version tag es inválido.");
    }

    @Test
    @DisplayName("shouldFailStreamValidationWhenVersionTagIsMissing")
    void shouldFailStreamValidationWhenVersionTagIsMissing() {
        // Arrange
        String bpmnXml = createBpmnXml(null);
        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, activeTopics, vipRoles);

        // Assert
        assertFalse(response.isValid(), "El BPMN sin version tag debe ser rechazado.");
        boolean hasError = response.getErrors().stream()
                .anyMatch(err -> err.getMessage().contains("obligatorio") || err.getMessage().contains("versionTag"));
        assertTrue(hasError, "Debe reportar un error indicando que falta el version tag.");
    }

    // ── Draft XML Validation Tests (validateDraftXml) ────────────────────────

    @Test
    @DisplayName("shouldPassDraftValidationWhenVersionTagIsValid")
    void shouldPassDraftValidationWhenVersionTagIsValid() {
        // Arrange
        String bpmnXml = createBpmnXml("1.0.0");

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(bpmnXml, 100);

        // Assert
        assertTrue(result.isPassed(), "La pre-validación del borrador con tag válido debe pasar.");
    }

    @Test
    @DisplayName("shouldFailDraftValidationWhenVersionTagIsInvalid")
    void shouldFailDraftValidationWhenVersionTagIsInvalid() {
        // Arrange
        String bpmnXml = createBpmnXml("invalid-version");

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(bpmnXml, 100);

        // Assert
        assertFalse(result.isPassed(), "La pre-validación debe fallar para tags no conformes a SemVer.");
        boolean hasErrorIssue = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) && issue.getMessage().contains("SemVer"));
        assertTrue(hasErrorIssue, "Debe registrar un issue de error referente al formato de versión.");
    }

    @Test
    @DisplayName("shouldFailDraftValidationWhenVersionTagIsMissing")
    void shouldFailDraftValidationWhenVersionTagIsMissing() {
        // Arrange
        String bpmnXml = createBpmnXml(null);

        // Act
        PreFlightResultDTO result = adapter.validateDraftXml(bpmnXml, 100);

        // Assert
        assertFalse(result.isPassed(), "La pre-validación debe fallar ante la ausencia del tag de versión.");
        boolean hasErrorIssue = result.getIssues().stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()) && (issue.getMessage().contains("obligatoria") || issue.getMessage().contains("versionTag")));
        assertTrue(hasErrorIssue, "Debe registrar un issue de error sobre la propiedad faltante.");
    }
}
