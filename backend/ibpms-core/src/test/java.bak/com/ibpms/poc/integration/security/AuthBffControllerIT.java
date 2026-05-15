package com.ibpms.poc.integration.security;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleTemplateRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleHierarchyRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthBffControllerIT extends AbstractIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RoleTemplateRepository roleTemplateRepository;

    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        roleHierarchyRepository.deleteAll();
        roleTemplateRepository.deleteAll();

        RoleTemplateEntity managerRole = new RoleTemplateEntity();
        managerRole.setRoleName("ROLE_MANAGER");
        managerRole.setDescription("Manager");
        RoleTemplateEntity employeeRole = new RoleTemplateEntity();
        employeeRole.setRoleName("ROLE_EMPLOYEE");
        employeeRole.setDescription("Employee");
        roleTemplateRepository.saveAll(List.of(managerRole, employeeRole));

        RoleHierarchyEntity hierarchy = new RoleHierarchyEntity();
        hierarchy.setParentRole(managerRole);
        hierarchy.setChildRole(employeeRole);
        roleHierarchyRepository.save(hierarchy);
    }

    @Test
    void getEffectiveRolesWithInheritance_Success() {
        String token = jwtTokenProvider.generateToken("manager_user", List.of("MANAGER"), "tenant1");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/auth/effective-roles")
        .then()
            .statusCode(200)
            .body("$", hasItems("MANAGER", "EMPLOYEE"));
    }

    @Test
    void getEffectiveRolesNoAuth_Unauthorized() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/auth/effective-roles")
        .then()
            .statusCode(401);
    }
}
