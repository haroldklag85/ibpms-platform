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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ExternalTaskTopicsCatalogTest {

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
    @DisplayName("CA-70: testGetTopicsReturnsSeedData")
    void testGetTopicsReturnsSeedData() {
        given()
        .when()
            .get("/topics")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(6));
    }

    @Test
    @DisplayName("CA-70: testPreFlightRejectsUnknownTopic")
    void testPreFlightRejectsUnknownTopic() {
        // Enviar BPMN que contiene <camunda:task topic="inventado"/> para fallar en la validación
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:serviceTask id=\"Task_1\" camunda:type=\"external\" camunda:topic=\"inventado\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .multiPart("file", "test.bpmn", bpmnContent.getBytes())
        .when()
            .post("/validate")
        .then()
            .statusCode(200) // Endpoint returns 200 with list of errors
            .body("errors", hasItem(containsString("inventado")));
    }
}
