// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.port.out;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;

import java.util.List;
import java.util.Set;

public interface MenuTopologyPort {
    List<MenuItemDTO> findMenuTreeByRoles(Set<String> roles);
}
