package com.ibpms.poc.application.dto;

import java.util.Set;

public class MenuTopologyDTO {
    private Set<String> activeMenus;

    public MenuTopologyDTO() {}

    public MenuTopologyDTO(Set<String> activeMenus) {
        this.activeMenus = activeMenus;
    }

    public Set<String> getActiveMenus() {
        return activeMenus;
    }

    public void setActiveMenus(Set<String> activeMenus) {
        this.activeMenus = activeMenus;
    }
}
