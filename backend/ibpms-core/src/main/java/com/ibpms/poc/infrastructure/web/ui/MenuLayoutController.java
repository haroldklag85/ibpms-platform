package com.ibpms.poc.infrastructure.web.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import com.ibpms.poc.application.service.ui.MenuLayoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Adapter-In (Controller) Hexagonal para despachar el Layout al SPA.
 */
@RestController
@RequestMapping("/api/v1/users/me")
public class MenuLayoutController {

    private final MenuLayoutService menuLayoutService;

    public MenuLayoutController(MenuLayoutService menuLayoutService) {
        this.menuLayoutService = menuLayoutService;
    }

    /**
     * Endpoint CA-6 V1.
     * Recupera el Árbol dinámico aislando el Token en el Context de Spring (State-less).
     */
    @GetMapping("/menu-layout")
    public ResponseEntity<List<MenuItemDTO>> getMenuLayout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Set<String> activeModules = menuLayoutService.computeTopologyForUser(username);

        List<MenuItemDTO> menu = new java.util.ArrayList<>();

        if (activeModules.contains("WORKDESK") || activeModules.contains("SERVICE_DELIVERY")) {
            MenuItemDTO delivery = new MenuItemDTO("Service Delivery", "mdi-account-group", null);
            if (activeModules.contains("WORKDESK")) delivery.addChild(new MenuItemDTO("Workdesk", "mdi-view-dashboard-variant", "/workdesk"));
            if (activeModules.contains("SERVICE_DELIVERY")) delivery.addChild(new MenuItemDTO("Formularios", "mdi-text-box-plus", "/admin/modeler/forms"));
            menu.add(delivery);
        }

        if (activeModules.contains("PROJECTS") || activeModules.contains("MODELER")) {
            MenuItemDTO builder = new MenuItemDTO("Project Builder", "mdi-hammer-wrench", null);
            if (activeModules.contains("PROJECTS")) builder.addChild(new MenuItemDTO("Proyectos", "mdi-folder-lock", "/admin/projects/manager"));
            if (activeModules.contains("MODELER")) builder.addChild(new MenuItemDTO("Modelador BPMN", "mdi-sitemap", "/admin/modeler/bpmn"));
            menu.add(builder);
        }

        if (activeModules.contains("BAM")) {
            MenuItemDTO analytics = new MenuItemDTO("Analytics & BAM", "mdi-chart-bar", null);
            analytics.addChild(new MenuItemDTO("Reportes Básicos", "mdi-chart-timeline-variant", "/admin/analytics/bam"));
            analytics.addChild(new MenuItemDTO("BAM Dashboard", "mdi-monitor-dashboard", "/admin/analytics/bam"));
            menu.add(analytics);
        }

        if (activeModules.contains("INTEGRATION")) {
            MenuItemDTO integration = new MenuItemDTO("Integration Hub", "mdi-api", null);
            integration.addChild(new MenuItemDTO("Catálogo", "mdi-book-open-page-variant", "/admin/integration/catalog"));
            integration.addChild(new MenuItemDTO("Builder", "mdi-puzzle-edit", "/admin/integration/builder"));
            menu.add(integration);
        }

        if (activeModules.contains("ADMINISTRATION")) {
            MenuItemDTO governance = new MenuItemDTO("Gobernanza", "mdi-shield-alert", null);
            governance.addChild(new MenuItemDTO("Gobernanza de Identidad", "mdi-account-details", "/admin/security/identity"));
            governance.addChild(new MenuItemDTO("PMO", "mdi-alert-octagon", "/admin/pmo/settings"));
            menu.add(governance);
        }

        return ResponseEntity.ok(menu);
    }
}
