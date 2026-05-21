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

    @GetMapping("/me/menu-layout")
    public org.springframework.http.ResponseEntity<List<com.ibpms.poc.application.dto.ui.MenuItemDTO>> getMenuLayout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Set<String> activeModules = menuLayoutService.computeTopologyForUser(username);

        List<com.ibpms.poc.application.dto.ui.MenuItemDTO> menu = new java.util.ArrayList<>();

        if (activeModules.contains("WORKDESK") || activeModules.contains("SERVICE_DELIVERY")) {
            com.ibpms.poc.application.dto.ui.MenuItemDTO delivery = new com.ibpms.poc.application.dto.ui.MenuItemDTO("Service Delivery", "mdi-account-group", null);
            if (activeModules.contains("WORKDESK")) delivery.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Workdesk", "mdi-view-dashboard-variant", "/workdesk"));
            if (activeModules.contains("SERVICE_DELIVERY")) delivery.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Formularios", "mdi-text-box-plus", "/admin/modeler/forms"));
            menu.add(delivery);
        }

        if (activeModules.contains("PROJECTS") || activeModules.contains("MODELER")) {
            com.ibpms.poc.application.dto.ui.MenuItemDTO builder = new com.ibpms.poc.application.dto.ui.MenuItemDTO("Project Builder", "mdi-hammer-wrench", null);
            if (activeModules.contains("PROJECTS")) builder.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Proyectos", "mdi-folder-lock", "/admin/projects/manager"));
            if (activeModules.contains("MODELER")) builder.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Modelador BPMN", "mdi-sitemap", "/admin/modeler/bpmn"));
            menu.add(builder);
        }

        if (activeModules.contains("BAM")) {
            com.ibpms.poc.application.dto.ui.MenuItemDTO analytics = new com.ibpms.poc.application.dto.ui.MenuItemDTO("Analytics & BAM", "mdi-chart-bar", null);
            analytics.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Reportes Básicos", "mdi-chart-timeline-variant", "/admin/analytics/bam"));
            analytics.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("BAM Dashboard", "mdi-monitor-dashboard", "/admin/analytics/bam"));
            menu.add(analytics);
        }

        if (activeModules.contains("INTEGRATION")) {
            com.ibpms.poc.application.dto.ui.MenuItemDTO integration = new com.ibpms.poc.application.dto.ui.MenuItemDTO("Integration Hub", "mdi-api", null);
            integration.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Catálogo", "mdi-book-open-page-variant", "/admin/integration/catalog"));
            integration.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Builder", "mdi-puzzle-edit", "/admin/integration/builder"));
            menu.add(integration);
        }

        if (activeModules.contains("ADMINISTRATION")) {
            com.ibpms.poc.application.dto.ui.MenuItemDTO governance = new com.ibpms.poc.application.dto.ui.MenuItemDTO("Gobernanza", "mdi-shield-alert", null);
            governance.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("Gobernanza de Identidad", "mdi-account-details", "/admin/security/identity"));
            governance.addChild(new com.ibpms.poc.application.dto.ui.MenuItemDTO("PMO", "mdi-alert-octagon", "/admin/pmo/settings"));
            menu.add(governance);
        }

        return org.springframework.http.ResponseEntity.ok(menu);
    }


}
