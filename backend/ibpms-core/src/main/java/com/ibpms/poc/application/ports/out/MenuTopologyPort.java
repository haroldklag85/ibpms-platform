package com.ibpms.poc.application.ports.out;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;

import java.util.List;
import java.util.Set;

public interface MenuTopologyPort {
    List<MenuItemDTO> findMenuTreeByRoles(Set<String> roles);
}
