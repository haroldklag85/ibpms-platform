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

// @Traceability: US-005, CA-05
@DisplayName("CA-05: Deploy Nomenclature Governance - Missing ReglaNomenclatura")
public class DeployNomenclatureGovernanceCA05IT extends AbstractIntegrationIT {

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
        RestAssured.basePath = "";

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
    @DisplayName("Debe denegar el despliegue y retornar 422 cuando falta la propiedad de extension ReglaNomenclatura")
    void shouldDenyDeploymentAndReturn422WhenMissingNomenclatureProperty() {
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn2:process id=\"Process_CA05_NoNomenclature\" isExecutable=\"true\">\n" +
                "    <bpmn2:startEvent id=\"StartEvent_1\" camunda:formKey=\"startForm\" />\n" +
                "    <bpmn2:endEvent id=\"EndEvent_1\" />\n" +
                "    <bpmn2:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\" />\n" +
                "  </bpmn2:process>\n" +
                "</bpmn2:definitions>";

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .multiPart("file", "no_nomenclature.bpmn", bpmnContent.getBytes(), "text/xml")
            .formParam("deploy_comment", "Comentario de despliegue con mas de 10 caracteres")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            .log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body(containsString("Debe definir cómo se llamarán los casos de este proceso."));
    }
}
