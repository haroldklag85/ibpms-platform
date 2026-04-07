package com.ibpms.poc.infrastructure.web.bpmn;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BpmnDeployContractTest {

    @LocalServerPort
    private int port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ibpms_test")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/bpmn";
    }

    @Test
    @DisplayName("CA-65: testDeployWithValidCommentReturns201")
    void testDeployWithValidCommentReturns201() {
        given()
            .multiPart("file", "dummy.bpmn", "<bpmn/>".getBytes())
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
            .multiPart("file", "dummy.bpmn", "<bpmn/>".getBytes())
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testDeployCommentTooShortReturns400")
    void testDeployCommentTooShortReturns400() {
        given()
            .multiPart("file", "dummy.bpmn", "<bpmn/>".getBytes())
            .multiPart("deploy_comment", "Corta") // 5 chars
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testValidateEndpointDoesNotDeploy")
    void testValidateEndpointDoesNotDeploy() {
        given()
            .multiPart("file", "dummy.bpmn", "<bpmn/>".getBytes())
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
            .multiPart("file", "large.bpmn", largeFile)
            .multiPart("deploy_comment", "Deploy archivo extremadamente grande")
        .when()
            .post("/deploy")
        .then()
            .statusCode(413);
    }
}
