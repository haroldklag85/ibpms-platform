package com.ibpms.poc.application.usecase.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Port-In (UseCase) Hexagonal para orquestar la UI Dinámica.
 */
@Service
public class MenuLayoutUseCase {

    /**
     * Construye el Árbol de Rendereado de Menú (CA-6).
     * @param userRoles Roles del JWT extraídos del SecurityContext.
     * @return Arbol JSON de Menús desprovisto de rutas huérfanas o prohibidas.
     */
    public List<MenuItemDTO> getBuildLayoutForUser(Set<String> userRoles) {
        List<MenuItemDTO> layout = new ArrayList<>();

        // RAMA 0: Home (Acceso Universal)
        MenuItemDTO home = new MenuItemDTO("Inicio", "mdi-home", "/home");
        layout.add(home);

        // RAMA 1: Operativa Base (Todos tienen acceso a su Workdesk)
        MenuItemDTO dashboard = new MenuItemDTO("Mi Workdesk", "mdi-desktop-mac", "/workdesk");
        layout.add(dashboard);

        // RAMA 2: Aprobadores y Jefatura
        if (userRoles.contains("ROLE_APROBADOR_FINANCIERO") || userRoles.contains("ROLE_ALTA_DIRECCION")) {
            MenuItemDTO approvals = new MenuItemDTO("Aprobaciones Pendientes", "mdi-check-decagram", "/approvals");
            layout.add(approvals);
        }

        // RAMA 3: Configuración y Gobernanza
        MenuItemDTO settingsFolder = new MenuItemDTO("Administración y Gobernanza", "mdi-cog-box", null);

        if (userRoles.contains("ROLE_SUPER_ADMIN")) {
            settingsFolder.addChild(new MenuItemDTO("Generador de Entidades MDE", "mdi-database-plus", "/config/mde"));
            settingsFolder.addChild(new MenuItemDTO("Centro de IA (MLOps)", "mdi-brain", "/config/ai-center"));
            settingsFolder.addChild(new MenuItemDTO("Gestor de Festivos", "mdi-calendar-alert", "/config/holidays"));
        }

        if (userRoles.contains("ROLE_CISO") || userRoles.contains("ROLE_SUPER_ADMIN")) {
            settingsFolder.addChild(new MenuItemDTO("Tablero de Anomalías de Seguridad", "mdi-shield-alert", "/security/anomalies"));
            settingsFolder.addChild(new MenuItemDTO("Matriz Transaccional SoD", "mdi-file-tree", "/security/sod-matrix"));
        }

        // Regla de Poda Inteligente: Sólo agregar la carpeta "Configuración" si tiene al menos un hijo.
        if (!settingsFolder.getChildren().isEmpty()) {
            layout.add(settingsFolder);
        }

        if (userRoles.contains("ROLE_SUPER_ADMIN") || userRoles.contains("Global Admin")) {
            
            // RAMA: Service Delivery
            MenuItemDTO serviceDelivery = new MenuItemDTO("Service Delivery", "mdi-account-group", null);
            serviceDelivery.addChild(new MenuItemDTO("Triaje Intake", "mdi-filter", "/intake-triage"));
            serviceDelivery.addChild(new MenuItemDTO("Intake Manual", "mdi-text-box-plus", "/admin/intake"));
            serviceDelivery.addChild(new MenuItemDTO("Customer 360", "mdi-account-details", "/admin/customer360"));
            layout.add(serviceDelivery);

            // RAMA: Project Builder
            MenuItemDTO projectBuilder = new MenuItemDTO("Project Builder", "mdi-rocket", null);
            projectBuilder.addChild(new MenuItemDTO("Project Builder", "mdi-hammer-wrench", "/admin/project-builder"));
            projectBuilder.addChild(new MenuItemDTO("Gestor de Proyectos", "mdi-view-dashboard-variant", "/admin/projects/manager"));
            projectBuilder.addChild(new MenuItemDTO("Agile Hub", "mdi-chart-timeline-variant", "/admin/projects/agile-hub"));
            layout.add(projectBuilder);

            // RAMA: Analytics & BAM
            MenuItemDTO analytics = new MenuItemDTO("Analytics & BAM", "mdi-chart-bar", null);
            analytics.addChild(new MenuItemDTO("Dashboard BAM", "mdi-monitor-dashboard", "/admin/analytics/bam"));
            layout.add(analytics);

            // RAMA: Integration Hub
            MenuItemDTO integration = new MenuItemDTO("Integration Hub", "mdi-api", null);
            integration.addChild(new MenuItemDTO("Catálogo de Conectores", "mdi-book-open-page-variant", "/admin/integration/catalog"));
            integration.addChild(new MenuItemDTO("Connector Builder", "mdi-puzzle-edit", "/admin/integration/builder"));
            integration.addChild(new MenuItemDTO("Visual Mapper", "mdi-sitemap", "/admin/integration/mapper"));
            integration.addChild(new MenuItemDTO("DLQ Dashboard", "mdi-alert-octagon", "/admin/integration/dlq"));
            layout.add(integration);

            // RAMA: SGDEA
            MenuItemDTO sgdea = new MenuItemDTO("SGDEA", "mdi-folder-lock", null);
            sgdea.addChild(new MenuItemDTO("Bóveda Documental", "mdi-safe", "/sgdea/vault"));
            layout.add(sgdea);

            // RAMA: Gobernanza e Identidades
            MenuItemDTO governance = new MenuItemDTO("Gobernanza", "mdi-gavel", null);
            governance.addChild(new MenuItemDTO("Gobernanza de Identidades", "mdi-card-account-details", "/admin/security/identity"));
            governance.addChild(new MenuItemDTO("PMO / SLA Management", "mdi-timer-settings", "/admin/pmo/settings"));
            layout.add(governance);
        }

        return layout;
    }
}
