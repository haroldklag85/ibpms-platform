package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.application.port.out.RbacPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesplegarDefinicionServiceTest {

    @Mock
    private ProcesoBpmPort procesoBpmPort;

    @Mock
    private RbacPort rbacPort;

    @InjectMocks
    private DesplegarDefinicionService service;

    private DeploymentRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new DeploymentRequestDTO();
        validRequest.setResourceName("test_process.bpmn");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el XML está vacío")
    void desplegarDesdeWeb_EmptyXml() {
        validRequest.setXmlString("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.desplegarDesdeWeb(validRequest));

        assertEquals("El XML del modelo no puede estar vacío.", exception.getMessage());
        verify(procesoBpmPort, never()).desplegarProceso(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe desplegar BPMN y extraer Carriles (Lanes) correctametne para autogenerar Roles")
    void desplegarDesdeWeb_GenerarRolesExitosamente() {
        // XML Simulado con 1 proceso y 2 lanes
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <bpmn:process id=\"Proceso_Credito\" isExecutable=\"true\">\n" +
                "    <bpmn:laneSet id=\"LaneSet_1\">\n" +
                "      <bpmn:lane id=\"Lane_Analista\" name=\"Analista Riesgos\">\n" +
                "      </bpmn:lane>\n" +
                "      <bpmn:lane id=\"Lane_Gerente\" name=\"Gerente\">\n" +
                "      </bpmn:lane>\n" +
                "    </bpmn:laneSet>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        validRequest.setXmlString(bpmnXml);

        // Act
        service.desplegarDesdeWeb(validRequest);

        // Assert
        verify(procesoBpmPort, times(1)).desplegarProceso(eq("test_process.bpmn"), eq(bpmnXml));

        // Verifica que se llamó a la creación de rol para el Analista
        verify(rbacPort, times(1)).bindLaneToProfile(
                eq("Proceso_Credito"),
                eq("Lane_Analista"),
                eq("BPMN_Proceso_Credito_Analista_Riesgos"),
                eq("Autogenerado desde el Carril 'Analista Riesgos' del proceso 'Proceso_Credito'"));

        // Verifica que se llamó a la creación de rol para el Gerente
        verify(rbacPort, times(1)).bindLaneToProfile(
                eq("Proceso_Credito"),
                eq("Lane_Gerente"),
                eq("BPMN_Proceso_Credito_Gerente"),
                eq("Autogenerado desde el Carril 'Gerente' del proceso 'Proceso_Credito'"));
    }

    @Test
    @DisplayName("Debe desplegar DMN sin intentar extraer Roles (fail graceful)")
    void desplegarDesdeWeb_DmnSinErrores() {
        // Arrange
        validRequest.setResourceName("tabla_decision.dmn");
        validRequest.setXmlString("<definitions id=\"dmn_1\"></definitions>");

        // Act
        service.desplegarDesdeWeb(validRequest);

        // Assert
        verify(procesoBpmPort, times(1)).desplegarProceso("tabla_decision.dmn",
                "<definitions id=\"dmn_1\"></definitions>");
        verify(rbacPort, never()).bindLaneToProfile(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe desplegar BPMN y continuar aunque el XML sea malformado e interrumpa la extracción de Roles")
    void desplegarDesdeWeb_XmlMalformadoFallback() {
        // Arrange - XML roto
        String brokenXml = "<bpmn:process id=\"";
        validRequest.setXmlString(brokenXml);

        // Act
        service.desplegarDesdeWeb(validRequest);

        // Assert - se despliega igual, el puerto no se llama
        verify(procesoBpmPort, times(1)).desplegarProceso("test_process.bpmn", brokenXml);
        verify(rbacPort, never()).bindLaneToProfile(anyString(), anyString(), anyString(), anyString());
    }
}
