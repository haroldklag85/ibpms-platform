package com.ibpms.poc.infrastructure.web.dmn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DmnValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-007 CA-7: Aserción Hard-Stop sobre Tablas de Decisión que superen las 50 Filas (HTTP 400)")
    void testDmnUpload_Exceeding50Rules_ThrowsHttp400() throws Exception {
        // Simulamos un XML DMN malicioso o hiper-extenso con 51 Reglas
        StringBuilder dmnPayload = new StringBuilder();
        dmnPayload.append("<decisionTable id=\"test\">\n");
        for (int i = 0; i < 51; i++) {
            dmnPayload.append("  <rule id=\"rule_").append(i).append("\">\n    <inputEntry><text>true</text></inputEntry>\n  </rule>\n");
        }
        dmnPayload.append("</decisionTable>");

        mockMvc.perform(post("/api/v1/dmn/upload")
                .header("X-Mock-Tester", "QA_Agent_51")
                .contentType(MediaType.APPLICATION_XML)
                .content(dmnPayload.toString()))
               .andExpect(status().isBadRequest()) // HTTP 400
               .andExpect(jsonPath("$.error").value("DMN_RULE_LIMIT_EXCEEDED"))
               .andExpect(jsonPath("$.message").value("El número de reglas de negocio no puede superar el límite estricto de 50. (Detectadas: 51)"));
    }

    @Test
    @DisplayName("US-007 CA-8: Aserción de Mutación Forzada ('UNIQUE' -> 'FIRST') previniendo bloqueos lógicos en Camunda")
    void testDmnUpload_MutatesUniqueHitPolicyToFirst() throws Exception {
        // XML original propuesto por el usuario en la GUI, requiriendo UNIQUE hit policy
        String originalDmn = "<decisionTable id=\"test\" hitPolicy=\"UNIQUE\">\n" +
                             "  <rule id=\"rule_0\"><inputEntry><text>true</text></inputEntry></rule>\n" +
                             "</decisionTable>";

        // El Controlador debe interceptar el XML, parsearlo y reemplazar "UNIQUE" con "FIRST".
        // Asumiendo que nuestro endpoint ficticio devuelve el XML resultante o un objeto de confirmación
        mockMvc.perform(post("/api/v1/dmn/upload")
                .header("X-Mock-Tester", "QA_Agent_51")
                .contentType(MediaType.APPLICATION_XML)
                .content(originalDmn))
               .andExpect(status().is2xxSuccessful())
               // En un escenario real, aserccionamos contra Base de Datos, pero aquí chequeamos el payload reflejado
               .andExpect(result -> {
                   String content = result.getResponse().getContentAsString();
                   // Aserción Matemática: La variable UNIQUE desapareció para siempre del XML.
                   assert !content.contains("hitPolicy=\"UNIQUE\"");
                   assert content.contains("hitPolicy=\"FIRST\"");
               });
    }
}
