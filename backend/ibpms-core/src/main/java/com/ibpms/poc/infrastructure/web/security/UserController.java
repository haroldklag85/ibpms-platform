package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.service.security.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint público/operacional para el catálogo de usuarios.
 * Sigue el principio de Mínimo Privilegio (Zero-Trust), exponiendo únicamente
 * los datos necesarios (ID, Nombre, Email, Roles) para las asignaciones y delegaciones
 * en el Workdesk y los dropdowns del frontend, sin comprometer hashes o parámetros críticos.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.listAll());
    }
}
