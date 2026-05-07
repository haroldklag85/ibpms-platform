package com.ibpms.poc.application.usecase.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import com.ibpms.poc.application.ports.out.MenuTopologyPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Port-In (UseCase) Hexagonal para orquestar la UI Dinámica.
 */
@Service
public class MenuLayoutUseCase {

    private final MenuTopologyPort menuTopologyPort;

    public MenuLayoutUseCase(MenuTopologyPort menuTopologyPort) {
        this.menuTopologyPort = menuTopologyPort;
    }

    /**
     * Construye el Árbol de Rendereado de Menú (CA-6).
     * @param userRoles Roles del JWT extraídos del SecurityContext.
     * @return Arbol JSON de Menús desprovisto de rutas huérfanas o prohibidas.
     */
    public List<MenuItemDTO> getBuildLayoutForUser(Set<String> userRoles) {
        return menuTopologyPort.findMenuTreeByRoles(userRoles);
    }
}
