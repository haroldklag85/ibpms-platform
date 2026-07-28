package com.ibpms.poc.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.ibpms.poc.application.port.in.ProcesarEmailWebhookUseCase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class EmailWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProcesarEmailWebhookUseCase webhookUseCase;

    @InjectMocks
    private EmailWebhookController controller;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void receiveEmail_returnsGone() throws Exception {
        mockMvc.perform(post("/inbound/email-webhook")
                .header("ClientState", "secreto-compartido-m365")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"Test\"}"))
                .andExpect(status().isGone())
                .andExpect(content().string(containsString("ENDPOINT_DEPRECATED")))
                .andExpect(content().string(containsString("migration")));

        verifyNoInteractions(webhookUseCase);
    }
}
