package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CamundaBpmnValidationAdapterTest {

    private CamundaBpmnValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CamundaBpmnValidationAdapter();
    }

    @Test
    void validateBpmnStream_InvalidXml_ReturnsError() {
        InputStream stream = new ByteArrayInputStream("invalid xml".getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse response = adapter.validateBpmnStream(stream, List.of(), List.of());
        
        assertFalse(response.isValid());
        assertTrue(response.getErrors().stream().anyMatch(e -> e.contains("Error al parsear el modelo BPMN")));
    }
}
