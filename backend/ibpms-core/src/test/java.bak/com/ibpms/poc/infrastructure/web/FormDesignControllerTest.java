package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.dto.FormFieldMetadataDTO;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class FormDesignControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FormDesignService formDesignService;

    @InjectMocks
    private FormDesignController formDesignController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(formDesignController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    public void testCrearFormularioConMetadatosCamunda() throws Exception {
        // Arrange
        CreateFormDesignDTO createDTO = new CreateFormDesignDTO();
        createDTO.setName("Formulario Test Integracion");
        createDTO.setTechnicalName("integration-form-123");
        createDTO.setPattern("SIMPLE");
        createDTO.setVueTemplate("<template><h1>Hola</h1></template>");
        
        FormFieldMetadataDTO field1 = new FormFieldMetadataDTO();
        field1.setCamundaVariable("customerName");
        field1.setType("text");
        field1.setZodRule("z.string().min(2)");

        FormFieldMetadataDTO field2 = new FormFieldMetadataDTO();
        field2.setCamundaVariable("customerAge");
        field2.setType("number");
        field2.setZodRule("z.number().min(18)");

        createDTO.setFormFields(List.of(field1, field2));

        FormDesignDTO responseDto = new FormDesignDTO();
        responseDto.setId(UUID.randomUUID());
        responseDto.setTechnicalName("integration-form-123");
        responseDto.setVersion(1);
        responseDto.setFormFields(List.of(field1, field2));

        Mockito.when(formDesignService.crear(any(CreateFormDesignDTO.class), eq("harolt")))
                .thenReturn(responseDto);

        Mockito.when(formDesignService.obtenerVersionInmutable(eq("integration-form-123"), eq(1)))
                .thenReturn(responseDto);

        // Act & Assert (POST)
        mockMvc.perform(post("/api/v1/forms")
                .header("X-User-Id", "harolt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicalName").value("integration-form-123"))
                .andExpect(jsonPath("$.formFields[0].camundaVariable").value("customerName"))
                .andExpect(jsonPath("$.formFields[1].camundaVariable").value("customerAge"));

        // Assert (GET) Version inmutable
        mockMvc.perform(get("/api/v1/forms/integration-form-123/versions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formFields.length()").value(2))
                .andExpect(jsonPath("$.formFields[0].camundaVariable").value("customerName"));
    }
}
