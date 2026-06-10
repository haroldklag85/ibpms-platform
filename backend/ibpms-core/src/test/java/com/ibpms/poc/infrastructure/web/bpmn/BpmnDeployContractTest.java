// @Traceability: US-005, CA-65
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.springframework.boot.test.web.server.LocalServerPort;


public class BpmnDeployContractTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BpmnProcessDesignRepository processDesignRepository;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        this.token = jwtTokenProvider.generateToken("BPMN_Release_Manager", java.util.Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "tenant_alpha");
    }

    @Test
    @DisplayName("CA-65: testDeployWithValidCommentReturns201")
    void testDeployWithValidCommentReturns201() {
        // @Traceability: US-005, CA-15
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\" camunda:versionTag=\"1.0.0\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <camunda:property name=\"ReglaNomenclatura\" value=\"CASO-${GENERIC}\" />\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"sys_generic_form\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(validBpmn.getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Comentario de despliegue válido")
        .when()
            .post("/deploy")
        .then()
            .statusCode(201)
            .body("deployment_id", notNullValue())
            .body("version", notNullValue())
            .body("deployed_at", notNullValue())
            .body("deployed_by", notNullValue());
    }

    @Test
    @DisplayName("CA-65: testDeployWithoutCommentReturns400")
    void testDeployWithoutCommentReturns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder("<bpmn/>".getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testDeployCommentTooShortReturns400")
    void testDeployCommentTooShortReturns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder("<bpmn/>".getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Corta") // 5 chars
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testValidateEndpointDoesNotDeploy")
    void testValidateEndpointDoesNotDeploy() {
        // @Traceability: US-005, CA-15
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\" camunda:versionTag=\"1.0.0\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <camunda:property name=\"ReglaNomenclatura\" value=\"CASO-${GENERIC}\" />\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"sys_generic_form\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(validBpmn.getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
        .when()
            .post("/validate")
        .then()
            .statusCode(200)
            .body("deployment_id", nullValue())
            .body("errors", notNullValue());
    }

    @Test
    @DisplayName("CA-65: testDeployFileSizeExceeds5MBReturns413")
    void testDeployFileSizeExceeds5MBReturns413() {
        byte[] largeFile = new byte[6 * 1024 * 1024]; // 6MB
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(largeFile)
                    .controlName("file")
                    .fileName("large.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Deploy archivo extremadamente grande")
        .when()
            .post("/deploy")
        .then()
            .statusCode(413);
    }

    @Test
    @DisplayName("BUG-FIX: testGetVersionsForNonExistentProcessReturnsEmptyList")
    void testGetVersionsForNonExistentProcessReturnsEmptyList() {
        // @Traceability: US-005, BUG-FIX: Asegurar que procesos no creados retornen lista vacía y no error 500
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/non-existent-process-12345/versions")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(0));
    }

    @Test
    @DisplayName("BUG-FIX: testGetVersionsForExistentProcessReturnsAlignedFields")
    void testGetVersionsForExistentProcessReturnsAlignedFields() {
        // @Traceability: US-005, BUG-FIX: Asegurar contrato correcto de llaves (version, date, author, status)
        // @Traceability: US-005, CA-15
        String processKey = "process-test-versions-123";
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Def_1\">\n" +
                "  <bpmn:process id=\"" + processKey + "\" isExecutable=\"true\" camunda:versionTag=\"1.0.0\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Seed the process design in the database so that draft saving and version fetching works
        processDesignRepository.findByTechnicalId(processKey).ifPresent(p -> processDesignRepository.delete(p));

        BpmnProcessDesignEntity processDesign = new BpmnProcessDesignEntity();
        processDesign.setId(UUID.randomUUID());
        processDesign.setName("Test Process Versions");
        processDesign.setTechnicalId(processKey);
        processDesign.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        processDesign.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        processDesign.setCurrentVersion(1);
        processDesign.setMaxNodes(50);
        processDesign.setCreatedAt(LocalDateTime.now());
        processDesign.setUpdatedAt(LocalDateTime.now());
        processDesign.setCreatedBy("BPMN_Release_Manager");
        processDesignRepository.save(processDesign);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of("xml", validBpmn))
        .when()
            .put("/" + processKey + "/draft")
        .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/" + processKey + "/versions")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].version", notNullValue())
            .body("[0].date", notNullValue())
            .body("[0].author", notNullValue())
            .body("[0].status", notNullValue());
    }

    @Test
    @DisplayName("BUG-FIX: testGetProcessXmlNonExistentKeyReturnsDefaultTemplate200")
    void testGetProcessXmlNonExistentKeyReturnsDefaultTemplate200() {
        // @Traceability: US-005, CA-15
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/non-existent-process-key-abc/xml")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("xml", containsString("<bpmn:process id=\"non-existent-process-key-abc\""));
    }

    @Test
    @DisplayName("BUG-FIX: testGetProcessXmlEmptyDraftXmlReturnsDefaultTemplate200")
    void testGetProcessXmlEmptyDraftXmlReturnsDefaultTemplate200() {
        // @Traceability: US-005, CA-15
        String processKey = "process-test-empty-draft";
        
        processDesignRepository.findByTechnicalId(processKey).ifPresent(p -> processDesignRepository.delete(p));

        BpmnProcessDesignEntity processDesign = new BpmnProcessDesignEntity();
        processDesign.setId(UUID.randomUUID());
        processDesign.setName("Test Process Empty Draft");
        processDesign.setTechnicalId(processKey);
        processDesign.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        processDesign.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        processDesign.setCurrentVersion(1);
        processDesign.setMaxNodes(50);
        processDesign.setXmlDraft("   "); // blank XML
        processDesign.setCreatedAt(LocalDateTime.now());
        processDesign.setUpdatedAt(LocalDateTime.now());
        processDesign.setCreatedBy("BPMN_Release_Manager");
        processDesignRepository.save(processDesign);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/" + processKey + "/xml")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("xml", containsString("<bpmn:process id=\"" + processKey + "\""));
    }

    @Test
    @DisplayName("BUG-FIX: testGetProcessXmlExistentDraftXmlReturnsDraftXml200")
    void testGetProcessXmlExistentDraftXmlReturnsDraftXml200() {
        // @Traceability: US-005, CA-15
        String processKey = "process-test-with-draft";
        String customXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:definitions id=\"Custom_1\"></bpmn:definitions>";

        processDesignRepository.findByTechnicalId(processKey).ifPresent(p -> processDesignRepository.delete(p));

        BpmnProcessDesignEntity processDesign = new BpmnProcessDesignEntity();
        processDesign.setId(UUID.randomUUID());
        processDesign.setName("Test Process With Draft");
        processDesign.setTechnicalId(processKey);
        processDesign.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        processDesign.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        processDesign.setCurrentVersion(1);
        processDesign.setMaxNodes(50);
        processDesign.setXmlDraft(customXml);
        processDesign.setCreatedAt(LocalDateTime.now());
        processDesign.setUpdatedAt(LocalDateTime.now());
        processDesign.setCreatedBy("BPMN_Release_Manager");
        processDesignRepository.save(processDesign);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/" + processKey + "/xml")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("xml", equalTo(customXml));
    }

    @Test
    @DisplayName("BUG-FIX: testPutDraftForNonExistentProcessCreatesItSuccessfully")
    void testPutDraftForNonExistentProcessCreatesItSuccessfully() {
        // @Traceability: US-005, CA-15
        String processKey = "non-existent-process-put-draft-123";
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Def_1\">\n" +
                "  <bpmn:process id=\"" + processKey + "\" isExecutable=\"true\" camunda:versionTag=\"1.0.0\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Ensure it doesn't exist
        processDesignRepository.findByTechnicalId(processKey).ifPresent(p -> processDesignRepository.delete(p));

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of("xml", validBpmn))
        .when()
            .put("/" + processKey + "/draft")
        .then()
            .statusCode(200);

        // Verify it was created in repository
        org.junit.jupiter.api.Assertions.assertTrue(
            processDesignRepository.findByTechnicalId(processKey).isPresent()
        );
        
        // Clean up
        processDesignRepository.findByTechnicalId(processKey).ifPresent(p -> processDesignRepository.delete(p));
    }
}

