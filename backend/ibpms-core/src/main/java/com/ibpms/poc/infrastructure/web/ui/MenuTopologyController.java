package com.ibpms.poc.infrastructure.web.ui;

import com.ibpms.poc.application.dto.MenuTopologyDTO;
import com.ibpms.poc.application.service.ui.MenuLayoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class MenuTopologyController {

    private final MenuLayoutService menuLayoutService;

    public MenuTopologyController(MenuLayoutService menuLayoutService) {
        this.menuLayoutService = menuLayoutService;
    }

    @GetMapping("/menu-layout")
    public ResponseEntity<MenuTopologyDTO> getMyMenuTopology(@AuthenticationPrincipal UserDetails user) {
        // En un entorno JWT, el username suele ser el claim del token o puede venir como String si no usamos UserDetails
        String username = (user != null) ? user.getUsername() : "anonymous";
        return ResponseEntity.ok(menuLayoutService.computeTopologyForUser(username));
    }
}
