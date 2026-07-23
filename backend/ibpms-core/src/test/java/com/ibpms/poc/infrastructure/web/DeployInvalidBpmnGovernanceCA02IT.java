package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

// @Traceability: US-005, CA-02
@DisplayName("CA-02: Deploy Invalid BPMN Governance - Missing End Event")
public class DeployInvalidBpmnGovernanceCA02IT extends AbstractIntegrationIT {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity releaseManager;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;

        java.util.Optional<UserEntity> existingUser = userRepository.findByUsername("release_manager");
        if (existingUser.isPresent()) {
            releaseManager = existingUser.get();
        } else {
            RoleEntity releaseManagerRole = roleRepository.findByName("ROLE_BPMN_Release_Manager")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_BPMN_Release_Manager", "BPMN Release Manager")));

            releaseManager = new UserEntity();
            releaseManager.setUsername("release_manager");
            releaseManager.setEmail("rm@test.com");
            releaseManager.getRoles().add(releaseManagerRole);
            releaseManager = userRepository.save(releaseManager);
        }
    }

    private String getValidToken() {
        return jwtTokenProvider.generateToken(releaseManager.getUsername(), Collections.singletonList("ibpms_rol_BPMN_Release_Manager"), "T1");
    }

    @Test
    @DisplayName("Debe denegar el despliegue y retornar 422 cuando falta el End Event")
    void shouldDenyDeploymentAndReturn422WhenMissingEndEvent() {
        String invalidBpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn2:process id=\"Process_CA02_Invalid\" isExecutable=\"true\">\n" +
                "    <bpmn2:startEvent id=\"StartEvent_1\" />\n" +
                "  </bpmn2:process>\n" +
                "</bpmn2:definitions>";

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .multiPart("file", "invalid_process.bpmn", invalidBpmnContent.getBytes(), "text/xml")
            .formParam("deploy_comment", "Comentario de despliegue con mas de 10 caracteres")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            .log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body(containsString("El diagrama no es instanciable. Falta End Event."));
    }
}


