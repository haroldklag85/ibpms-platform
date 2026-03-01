package com.ibpms.poc.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.out.CrmClientPort;
import com.ibpms.poc.application.port.out.ExpedienteRepositoryPort;
import com.ibpms.poc.application.port.out.IdempotencyPort;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.domain.model.Expediente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateExpedienteServiceTest {

    @Mock
    private ExpedienteRepositoryPort repositoryPort;

    @Mock
    private ProcesoBpmPort procesoBpmPort;

    @Mock
    private CrmClientPort crmClientPort;

    @Mock
    private IdempotencyPort idempotencyPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreateExpedienteService service;

    private ExpedienteDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ExpedienteDTO();
        validRequest.setIdempotencyKey("test-idempotency-key-123");
        validRequest.setDefinitionKey("proceso_gestion_legal");
        validRequest.setBusinessKey("RAD-2026-001");
        validRequest.setType("LEGAL");

        Map<String, Object> variables = new HashMap<>();
        variables.put("clienteId", "CLI-999");
        validRequest.setVariables(variables);
    }

    @Test
    @DisplayName("Debe retornar expediente previo si la clave de idempotencia ya existe")
    void testCreate_IdempotencyKeyExists() throws Exception {
        // Arrange
        String jsonPrevio = "{\"id\":\"" + UUID.randomUUID().toString() + "\",\"status\":\"CREATED\"}";
        when(idempotencyPort.existe("test-idempotency-key-123")).thenReturn(true);
        when(idempotencyPort.obtenerResultado("test-idempotency-key-123")).thenReturn(jsonPrevio);

        ExpedienteDTO previoDto = new ExpedienteDTO();
        previoDto.setStatus("CREATED");
        when(objectMapper.readValue(jsonPrevio, ExpedienteDTO.class)).thenReturn(previoDto);

        // Act
        ExpedienteDTO result = service.create(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("CREATED", result.getStatus());

        // Verificar que no se llamó a la base de datos ni a Camunda
        verify(repositoryPort, never()).save(any());
        verify(procesoBpmPort, never()).iniciarProceso(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Debe crear un expediente nuevo consultando al CRM e iniciando Camunda")
    void testCreate_NewExpedienteWithCrm() throws Exception {
        // Arrange
        when(idempotencyPort.existe("test-idempotency-key-123")).thenReturn(false);

        Map<String, Object> crmData = new HashMap<>();
        crmData.put("segmento", "VIP");
        when(crmClientPort.obtenerMetadatosCliente("CLI-999")).thenReturn(crmData);

        when(procesoBpmPort.iniciarProceso(eq("proceso_gestion_legal"), eq("RAD-2026-001"), any()))
                .thenReturn("camunda-proc-inst-777");

        // Simular que el repositorio devuelve la entidad guardada con un ID
        Expediente savedExpediente = Expediente.iniciarNuevo("proceso_gestion_legal", "RAD-2026-001", "LEGAL", crmData);
        savedExpediente = savedExpediente.vincularProceso("camunda-proc-inst-777");
        // Reflection o setter no disponible, pero mockearemos la respuesta del save directamente

        when(repositoryPort.save(any(Expediente.class))).thenAnswer(invocation -> {
            Expediente e = invocation.getArgument(0);
            return e; // En un test real, podríamos inyectar un ID UUID válido aquí
        });

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"status\":\"CREATED\"}");

        // Act
        ExpedienteDTO result = service.create(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("proceso_gestion_legal", result.getDefinitionKey());
        
        // Verificaciones
        verify(crmClientPort, times(1)).obtenerMetadatosCliente("CLI-999");
        verify(procesoBpmPort, times(1)).iniciarProceso(eq("proceso_gestion_legal"), eq("RAD-2026-001"), any());
        verify(repositoryPort, times(1)).save(any(Expediente.class));
        verify(idempotencyPort, times(1)).registrar(eq("test-idempotency-key-123"), anyString());
    }
}
