package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.infrastructure.adapter.CamundaBpmnValidationAdapter;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeploymentGovernanceIntegrationAdversarialTest {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void shouldPassValidationWhenExclusiveGatewayHasDefaultFlow() {
        // Arrange
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("Process_2")
                .startEvent("startEvent")
                    .camundaFormKey("startForm")
                .exclusiveGateway("gw_with_default")
                .endEvent("endEvent")
                .done();
        
        ExclusiveGateway gw = modelInstance.getModelElementById("gw_with_default");
        SequenceFlow seqFlow = modelInstance.newInstance(SequenceFlow.class);
        seqFlow.setId("default_flow");
        // Connect the sequence flow
        gw.getOutgoing().add(seqFlow);
        gw.setDefault(seqFlow);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bpmn.writeModelToStream(outputStream, modelInstance);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        List<String> activeTopics = List.of();
        List<String> vipRoles = List.of();

        // Act
        DeploymentValidationResponse response = adapter.validateBpmnStream(inputStream, activeTopics, vipRoles);

        // Assert
        boolean hasGatewayError = response.getErrors().stream()
                .anyMatch(error -> error.getNode() != null && error.getNode().equals("gw_with_default") &&
                        error.getMessage().contains("Hard-Stop: ExclusiveGateway sin Flujo por Defecto"));
        assertFalse(hasGatewayError, "It should NOT return a Hard-Stop error for the ExclusiveGateway WITH a default flow");
    }
}
