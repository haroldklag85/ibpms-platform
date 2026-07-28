package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.service.FormDesignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FormDesignControllerUatTest {

    private MockMvc mockMvc;

    @Mock
    private FormDesignService formDesignService;

    @InjectMocks
    private FormDesignController formDesignController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(formDesignController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    public void getFormByTechnicalName_ReturnsForm_WhenFound() throws Exception {
        FormDesignDTO responseDto = new FormDesignDTO();
        responseDto.setTechnicalName("integration-form-123");
        responseDto.setVersion(1);

        Mockito.when(formDesignService.obtenerPorTechnicalName(eq("integration-form-123")))
                .thenReturn(Optional.of(responseDto));

        mockMvc.perform(get("/api/v1/forms/integration-form-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicalName").value("integration-form-123"));
    }

    @Test
    public void getFormByTechnicalName_Returns404_WhenNotFound() throws Exception {
        Mockito.when(formDesignService.obtenerPorTechnicalName(eq("unknown-form")))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/forms/unknown-form")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
