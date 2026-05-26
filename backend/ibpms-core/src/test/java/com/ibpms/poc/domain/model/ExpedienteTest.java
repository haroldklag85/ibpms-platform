package com.ibpms.poc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpedienteTest {

    @Test
    @DisplayName("Debe iniciar un nuevo expediente con estado ACTIVE e inicializar variables")
    void testIniciarNuevo() {
        // Arrange
        Map<String, Object> initialVariables = new HashMap<>();
        initialVariables.put("monto", 5000);

        // Act
        Expediente expediente = Expediente.iniciarNuevo("credito_process", "REQ-100", "CREDITO", initialVariables);

        // Assert
        assertNotNull(expediente.getId());
        assertEquals("credito_process", expediente.getDefinitionKey());
        assertEquals("REQ-100", expediente.getBusinessKey());
        assertEquals("CREDITO", expediente.getType());
        assertEquals(Expediente.ExpedienteStatus.ACTIVE, expediente.getStatus());
        assertEquals(5000, expediente.getVariables().get("monto"));
        assertNull(expediente.getProcessInstanceId());
    }

    @Test
    @DisplayName("Debe vincular el processInstanceId preservando la inmutabilidad")
    void testVincularProceso() {
        // Arrange
        Expediente original = Expediente.iniciarNuevo("credito_process", "REQ-100", "CREDITO", new HashMap<>());

        // Act
        Expediente actualizado = original.vincularProceso("camunda-inst-123");

        // Assert
        assertNotSame(original, actualizado); // Asegurar inmutabilidad (es otra instancia)
        assertEquals(original.getId(), actualizado.getId()); // Mantiene identidad
        assertNull(original.getProcessInstanceId());
        assertEquals("camunda-inst-123", actualizado.getProcessInstanceId());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta completar un expediente ya completado")
    void testCompletarExpedienteYaCompletado() {
        // Arrange
        Expediente expediente = Expediente.iniciarNuevo("credito_process", "REQ-100", "CREDITO", new HashMap<>());
        Expediente completado = expediente.completar();

        // Act & Assert
        assertEquals(Expediente.ExpedienteStatus.COMPLETED, completado.getStatus());

        IllegalStateException exception = assertThrows(IllegalStateException.class, completado::completar);
        assertEquals("El expediente ya está completado.", exception.getMessage());
    }
}
