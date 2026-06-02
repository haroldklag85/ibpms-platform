package com.ibpms.poc.infrastructure.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

/**
 * Integration test for US-003 CA-81 (Version anchoring for in-flight processes/tasks).
 *
 * @Traceability: US-003 - CA-81
 */
@Traceability(US = "US-003", CA = {"CA-81"})
public class FormBffIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FormDefinitionRepository formDefinitionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TaskService taskService;

    private String token;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        // Generate a test JWT token
        token = jwtTokenProvider.generateToken(
            "test-qa-user",
            List.of("ibpms_rol_USER"),
            "default",
            Map.of()
        );
    }

    @Test
    public void testFormContextVersionAnchoringInFlightTask() {
        // [🕵️ QA - E2E]
        UUID formId = UUID.randomUUID();
        String taskId = UUID.randomUUID().toString();

        // 1. Persist Version 1 (V1) created 5 days ago
        FormDefinitionEntity defV1 = new FormDefinitionEntity();
        defV1.setId(UUID.randomUUID());
        defV1.setFormId(formId);
        defV1.setVersionId(1);
        defV1.setSchemaContent("{\"schema\": \"V1\", \"fields\": [\"customerName\"]}");
        defV1.setCreatedBy("system");
        defV1.setHashSha256("hash-v1-" + formId.toString().substring(0, 8));
        defV1.setCreatedAt(LocalDateTime.now().minusDays(5));
        formDefinitionRepository.save(defV1);

        // 2. Persist Version 2 (V2) created 1 day ago
        FormDefinitionEntity defV2 = new FormDefinitionEntity();
        defV2.setId(UUID.randomUUID());
        defV2.setFormId(formId);
        defV2.setVersionId(2);
        defV2.setSchemaContent("{\"schema\": \"V2\", \"fields\": [\"customerName\", \"customerAge\"]}");
        defV2.setCreatedBy("system");
        defV2.setHashSha256("hash-v2-" + formId.toString().substring(0, 8));
        defV2.setCreatedAt(LocalDateTime.now().minusDays(1));
        formDefinitionRepository.save(defV2);

        // 3. Mock task created 3 days ago (after V1 but before V2)
        Date taskCreateTime = java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(3));
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn(taskId);
        when(mockTask.getProcessInstanceId()).thenReturn("process-instance-ca81");
        when(mockTask.getFormKey()).thenReturn(formId.toString());
        when(mockTask.getCreateTime()).thenReturn(taskCreateTime);
        when(mockTask.getName()).thenReturn("QA Anchoring Task");

        TaskQuery mockQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(mockQuery);
        when(mockQuery.taskId(anyString())).thenReturn(mockQuery);
        when(mockQuery.singleResult()).thenReturn(mockTask);
        when(taskService.getVariables(anyString())).thenReturn(new HashMap<>());

        // 4. Request BFF form context - must strictly return V1 schema and version
        // This is expected to FAIL in the RED phase because BFF currently hardcodes "v1.2.0"
        // and doesn't fetch version dynamic relative to task instantiation time.
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/v1/workbox/tasks/{id}/form-context", taskId)
            .then()
            .statusCode(200)
            .body("schema_version", equalTo("1"))
            .body("layout.schema", equalTo("V1"));
    }
}
