package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.service.security.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ibpms.poc.application.dto.MenuTopologyDTO;
import com.ibpms.poc.application.service.ui.MenuLayoutService;

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
    private final MenuLayoutService menuLayoutService;

    public UserController(UserService userService, MenuLayoutService menuLayoutService) {
        this.userService = userService;
        this.menuLayoutService = menuLayoutService;
    }

    @GetMapping
    public org.springframework.http.ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return org.springframework.http.ResponseEntity.ok(userService.listAll());
    }

    @GetMapping("/me/menu-layout")
    public org.springframework.http.ResponseEntity<MenuTopologyDTO> getMyMenuLayout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return org.springframework.http.ResponseEntity.ok(menuLayoutService.computeTopologyForUser(username));
    }
}
