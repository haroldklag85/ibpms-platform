package com.ibpms.poc.integration.ui;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MenuLayoutControllerIT extends AbstractIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        // La tabla ibpms_menu_topology debería haber sido creada por Liquibase
        // Insertamos data manual en caso de que no haya o para asegurar tests
        jdbcTemplate.execute("DELETE FROM ibpms_menu_topology");
        jdbcTemplate.execute("INSERT INTO ibpms_menu_topology (id, parent_id, label, icon, path, required_roles, sort_order) VALUES " +
                "(1, null, 'Home', 'home', '/home', '[\"OPERARIO\", \"SUPERVISOR\", \"SUPER_ADMIN\"]', 1), " +
                "(2, null, 'Settings', 'settings', '/settings', '[\"SUPER_ADMIN\"]', 2)");
    }

    @Test
    void getMenuLayoutSuperAdmin_ReturnAll() {
        String adminToken = jwtTokenProvider.generateToken("superadmin", List.of("SUPER_ADMIN"), "tenant1");

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/users/me/menu-layout")
        .then()
            .statusCode(200)
            .body("$", hasSize(2)) // Home y Settings
            .body("label", hasItems("Home", "Settings"));
    }

    @Test
    void getMenuLayoutOperador_ReturnRestricted() {
        String opToken = jwtTokenProvider.generateToken("operador", List.of("OPERARIO"), "tenant1");

        given()
            .header("Authorization", "Bearer " + opToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/users/me/menu-layout")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].label", equalTo("Home"));
    }

    @Test
    void getMenuLayoutNoAuth_Unauthorized() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/users/me/menu-layout")
        .then()
            .statusCode(401); // 401 o 403 dependiente del config
    }
}
