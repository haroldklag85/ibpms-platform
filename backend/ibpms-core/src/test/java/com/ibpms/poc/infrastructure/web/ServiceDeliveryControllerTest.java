package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.ManualStartDTO;
import com.ibpms.poc.application.port.in.IniciarServicioManualUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ServiceDeliveryController.class)
public class ServiceDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IniciarServicioManualUseCase iniciarServicioManualUseCase;

    @Test
    @WithMockUser(username = "admin", roles = { "Admin_Intake" })
    void shouldStartManualServiceSuccessfully() throws Exception {
        UUID expectedId = UUID.randomUUID();
        when(iniciarServicioManualUseCase.iniciarServicio(any(ManualStartDTO.class))).thenReturn(expectedId);

        String payload = """
                    {
                        "definitionKey": "flujo-pqr",
                        "businessKey": "REQ-0001",
                        "type": "PQR",
                        "initialVariables": {
                            "monto": 5000
                        }
                    }
                """;

        mockMvc.perform(post("/service-delivery/manual-start")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.caseId").value(expectedId.toString()));
    }

    @Test
    @WithMockUser(username = "asesor", roles = {"Funcionario"})
    void shouldForbidManualStartWithoutAdminRole() throws Exception {
        String payload = """{"definitionKey": "flujo-pqr"}""";

        mockMvc.perform(post("/service-delivery/manual-start")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }
}
