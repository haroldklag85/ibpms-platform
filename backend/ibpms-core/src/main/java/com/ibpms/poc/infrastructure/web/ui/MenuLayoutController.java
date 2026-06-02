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

        // ==========================================
        // GRUPO A: dailyOperation (icon: mdi-desktop-mac)
        // ==========================================
        MenuItemDTO dailyOperation = new MenuItemDTO("groupA", "mdi-desktop-mac", null);
        if (activeModules.contains("WORKDESK")) {
            dailyOperation.addChild(new MenuItemDTO("portal", "mdi-home", "/"));
            dailyOperation.addChild(new MenuItemDTO("workdesk", "mdi-view-dashboard-variant", "/workdesk"));
            dailyOperation.addChild(new MenuItemDTO("kanban", "mdi-view-week", "/kanban"));
        }
        if (activeModules.contains("SERVICE_DELIVERY")) {
            dailyOperation.addChild(new MenuItemDTO("customer360", "mdi-account-details", "/admin/customer360"));
        }
        if (activeModules.contains("PROJECTS")) {
            dailyOperation.addChild(new MenuItemDTO("projectManager", "mdi-folder-lock", "/admin/projects/manager"));
            dailyOperation.addChild(new MenuItemDTO("agileHub", "mdi-chart-timeline-variant", "/admin/projects/agile-hub"));
        }
        if (activeModules.contains("SERVICE_DELIVERY")) {
            dailyOperation.addChild(new MenuItemDTO("intakeTriage", "mdi-filter", "/intake-triage"));
            dailyOperation.addChild(new MenuItemDTO("intakeManual", "mdi-text-box-plus", "/admin/intake"));
        }
        if (activeModules.contains("BAM")) {
            dailyOperation.addChild(new MenuItemDTO("bamDashboard", "mdi-monitor-dashboard", "/admin/analytics/bam"));
        }

        if (!dailyOperation.getChildren().isEmpty()) {
            menu.add(dailyOperation);
        }

        // ==========================================
        // GRUPO B: governanceSecurity (icon: mdi-shield-alert)
        // ==========================================
        if (activeModules.contains("ADMINISTRATION")) {
            MenuItemDTO governanceSecurity = new MenuItemDTO("groupB", "mdi-shield-alert", null);
            governanceSecurity.addChild(new MenuItemDTO("identityGovernance", "mdi-card-account-details", "/admin/security/identity"));
            governanceSecurity.addChild(new MenuItemDTO("pmoSettings", "mdi-timer-settings", "/admin/pmo/settings"));
            governanceSecurity.addChild(new MenuItemDTO("settings", "mdi-cog-box", "/admin"));
            if (!governanceSecurity.getChildren().isEmpty()) {
                menu.add(governanceSecurity);
            }
        }

        // ==========================================
        // GRUPO C: lowCodeDesign (icon: mdi-hammer-wrench)
        // ==========================================
        if (activeModules.contains("MODELER")) {
            MenuItemDTO lowCodeDesign = new MenuItemDTO("groupC", "mdi-hammer-wrench", null);
            lowCodeDesign.addChild(new MenuItemDTO("bpmnDesigner", "mdi-sitemap", "/admin/modeler/bpmn"));
            lowCodeDesign.addChild(new MenuItemDTO("formsList", "mdi-text-box-plus", "/admin/modeler/forms"));
            lowCodeDesign.addChild(new MenuItemDTO("formDesigner", "mdi-text-box-plus", "/admin/modeler/forms/designer"));
            lowCodeDesign.addChild(new MenuItemDTO("dmnCopilot", "mdi-gavel", "/admin/modeler/dmn"));
            lowCodeDesign.addChild(new MenuItemDTO("promptLibrary", "mdi-brain", "/ai/prompts"));
            lowCodeDesign.addChild(new MenuItemDTO("genericForm", "mdi-text-box-plus", "/admin/generic-form"));
            lowCodeDesign.addChild(new MenuItemDTO("visualMapper", "mdi-sitemap", "/admin/integration/mapper"));
            lowCodeDesign.addChild(new MenuItemDTO("projectBuilder", "mdi-rocket", "/admin/project-builder"));
            if (!lowCodeDesign.getChildren().isEmpty()) {
                menu.add(lowCodeDesign);
            }
        }

        // ==========================================
        // GRUPO D: integrationAutomation (icon: mdi-api)
        // ==========================================
        if (activeModules.contains("INTEGRATION")) {
            MenuItemDTO integrationAutomation = new MenuItemDTO("groupD", "mdi-api", null);
            integrationAutomation.addChild(new MenuItemDTO("connectorCatalog", "mdi-book-open-page-variant", "/admin/integration/catalog"));
            integrationAutomation.addChild(new MenuItemDTO("connectorBuilder", "mdi-puzzle-edit", "/admin/integration/builder"));
            integrationAutomation.addChild(new MenuItemDTO("dlqDashboard", "mdi-alert-octagon", "/admin/integration/dlq"));
            integrationAutomation.addChild(new MenuItemDTO("inboundMailboxes", "mdi-calendar-alert", "/admin/mailboxes"));
            integrationAutomation.addChild(new MenuItemDTO("documentVault", "mdi-safe", "/sgdea/vault"));
            integrationAutomation.addChild(new MenuItemDTO("incidentCenter", "mdi-shield-alert", "/admin/incidents"));
            integrationAutomation.addChild(new MenuItemDTO("instancesManager", "mdi-timer-settings", "/admin/modeler/instances"));
            if (!integrationAutomation.getChildren().isEmpty()) {
                menu.add(integrationAutomation);
            }
        }

        return ResponseEntity.ok(menu);
    }
}
