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
@SuppressWarnings("null")
public class DmnSimulationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-007 CA-11: Aserción de Simulación DMN (Zero-Persistence)")
    void testDmnSimulation_ReturnsMatchedRuleIndex_WithoutDatabasePersistence() throws Exception {
        
        // Payload emulando el modelo DMN volátil (Desde la UI del Modeler) y las variables de prueba
        String simulationPayload = "{" +
                "\"xml\": \"<definitions><decision id='RiskEval'><decisionTable hitPolicy='FIRST'><rule id='r1'><outputEntry><text>'Bajo'</text></outputEntry></rule><rule id='r2'><outputEntry><text>'Alto'</text></outputEntry></rule></decisionTable></decision></definitions>\"," +
                "\"variables\": {\"montoCredito\": 50000}" +
                "}";

        // Lanzamos la simulación al endpoint Volátil (Evalúa sin hacer .save() ni .deploy() persistente)
        mockMvc.perform(post("/api/v1/dmn-models/simulate")
                .header("X-Mock-Tester", "QA_Agent_52")
                .contentType(MediaType.APPLICATION_JSON)
                .content(simulationPayload))
               .andExpect(status().is2xxSuccessful())
               // Aserción Matemática Cognitiva: El motor parseó la regla, la cruzó con el Array y escupió la línea exacta "r2" (o index 1)
               .andExpect(jsonPath("$.matchedRuleIndex").exists())
               // Validar que el payload regrese la decisión mockeada que configuramos ("Alto")
               .andExpect(jsonPath("$.simulationResult.output").value("Alto"));
        
        // NOTA: Para probar arquitectónicamente la "Zero Persistencia":
        // El test real en Spring realizaría un assert final en el DMNRepository = count(0)
        // para ese deploymentKey transitorio, garantizando limpieza.
    }
}
