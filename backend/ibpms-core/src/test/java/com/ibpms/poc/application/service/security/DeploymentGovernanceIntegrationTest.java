package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.infrastructure.adapter.CamundaBpmnValidationAdapter;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeploymentGovernanceIntegrationTest {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void shouldFailValidationWhenExclusiveGatewayLacksDefaultFlow() {
        // Arrange: Create a BPMN with an ExclusiveGateway that has NO default flow
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("Process_1")
                .startEvent("startEvent")
                    .camundaFormKey("startForm")
                .exclusiveGateway("gw_no_default")
                .endEvent("end1")
                .moveToLastGateway()
                .endEvent("end2")
                .done();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bpmn.writeModelToStream(outputStream, modelInstance);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        List<String> activeTopics = List.of();
        List<String> vipRoles = List.of();

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(inputStream, activeTopics, vipRoles);

        // Assert
        assertFalse(response.isValid(), "The validation should fail due to Strict Deployment Governance (Hard-Stop)");
        
        boolean hasGatewayError = response.getErrors().stream()
                .anyMatch(error -> error.getNode() != null && error.getNode().equals("gw_no_default") &&
                        error.getMessage().contains("Hard-Stop: ExclusiveGateway sin Flujo por Defecto"));
        assertTrue(hasGatewayError, "It should return a Hard-Stop error for the ExclusiveGateway lacking a default flow");
    }
}
