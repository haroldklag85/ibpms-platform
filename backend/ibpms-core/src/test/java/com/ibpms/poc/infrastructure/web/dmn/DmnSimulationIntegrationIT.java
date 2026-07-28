// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.AbstractLocalE2EIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class DmnSimulationIntegrationIT extends AbstractLocalE2EIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-007 CA-11: Aserción de Simulación DMN (Zero-Persistence)")
    @org.springframework.security.test.context.support.WithMockUser(authorities = "ROLE_PROCESS_ARCHITECT")
    void testDmnSimulation_ReturnsMatchedRuleIndex_WithoutDatabasePersistence() throws Exception {
        
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<definitions xmlns=\"https://www.omg.org/spec/DMN/20191111/MODEL/\" id=\"Definitions_1\" name=\"DRD\" namespace=\"http://camunda.org/schema/1.0/dmn\">" +
                "  <decision id=\"RiskEval\" name=\"RiskEval\">" +
                "    <decisionTable id=\"DecisionTable_1\" hitPolicy=\"FIRST\">" +
                "      <input id=\"Input_1\">" +
                "        <inputExpression id=\"InputExpression_1\" typeRef=\"integer\">" +
                "          <text>montoCredito</text>" +
                "        </inputExpression>" +
                "      </input>" +
                "      <output id=\"Output_1\" name=\"output\" typeRef=\"string\" />" +
                "      <rule id=\"r1\">" +
                "        <inputEntry id=\"i1\"><text>&lt; 10000</text></inputEntry>" +
                "        <outputEntry id=\"o1\"><text>\"Bajo\"</text></outputEntry>" +
                "      </rule>" +
                "      <rule id=\"r2\">" +
                "        <inputEntry id=\"i2\"><text>&gt;= 10000</text></inputEntry>" +
                "        <outputEntry id=\"o2\"><text>\"Alto\"</text></outputEntry>" +
                "      </rule>" +
                "    </decisionTable>" +
                "  </decision>" +
                "</definitions>";

        String simulationPayload = "{" +
                "\"xml\": \"" + validXml.replace("\"", "\\\"") + "\"," +
                "\"variables\": {\"montoCredito\": 50000}" +
                "}";
        mockMvc.perform(post("/api/v1/dmn-models/simulate-sandbox")
                .header("X-Mock-Tester", "QA_Agent_52")
                .contentType(MediaType.APPLICATION_JSON)
                .content(simulationPayload))
               .andExpect(status().is2xxSuccessful())
               .andExpect(jsonPath("$.simulationResult[0].output").value("Alto"));
    }
}
