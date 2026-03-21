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

        return layout;
    }
}
