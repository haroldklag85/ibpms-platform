package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.service.security.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ibpms.poc.application.service.ui.MenuLayoutService;
import java.util.Set;
import java.util.List;

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




}
