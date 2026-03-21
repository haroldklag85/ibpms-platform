package com.ibpms.poc.infrastructure.web.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import com.ibpms.poc.application.usecase.ui.MenuLayoutUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter-In (Controller) Hexagonal para despachar el Layout al SPA.
 */
@RestController
@RequestMapping("/api/v1/users/me")
public class MenuLayoutController {

    private final MenuLayoutUseCase menuLayoutUseCase;

    public MenuLayoutController(MenuLayoutUseCase menuLayoutUseCase) {
        this.menuLayoutUseCase = menuLayoutUseCase;
    }

    /**
     * Endpoint CA-6 V1.
     * Recupera el Árbol dinámico aislando el Token en el Context de Spring (State-less).
     */
    @GetMapping("/menu-layout")
    public ResponseEntity<List<MenuItemDTO>> getMenuLayout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        List<MenuItemDTO> layout = menuLayoutUseCase.getBuildLayoutForUser(roles);

        return ResponseEntity.ok(layout);
    }
}
