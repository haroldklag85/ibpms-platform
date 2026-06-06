package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractLocalE2ETest;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Traceability(US = "US-005", CA = {"CA-68"})
@Transactional
public class DataMappingIntegrityTest extends AbstractLocalE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("CA-68: testSaveMappingPersistsInDatabase")
    void testSaveMappingPersistsInDatabase() throws Exception {
        // Payload compatible con BpmnDesignController.createDataMapping
        String mappingPayload = "{\"taskId\":\"Task_1\", \"connectorId\":\"conn-01\", \"mappingJson\":\"{\\\"bpmnVar\\\":\\\"monto\\\"}\"}";
        
        mockMvc.perform(post("/api/v1/design/processes/test-process/data-mappings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mappingPayload))
                .andExpect(status().isCreated());
            
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_data_mappings WHERE process_definition_key = 'test-process'", Integer.class);
        assertEquals(1, count, "El mapeo debe ser persistido en la base de datos");
    }
}
