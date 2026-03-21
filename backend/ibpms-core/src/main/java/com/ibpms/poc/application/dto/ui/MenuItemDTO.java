package com.ibpms.poc.application.dto.ui;

import java.util.ArrayList;
import java.util.List;

public class MenuItemDTO {
    private String title;
    private String icon;
    private String path;
    private List<MenuItemDTO> children;

    public MenuItemDTO(String title, String icon, String path) {
        this.title = title;
        this.icon = icon;
        this.path = path;
        this.children = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<MenuItemDTO> getChildren() { return children; }
    public void setChildren(List<MenuItemDTO> children) { this.children = children; }

    public void addChild(MenuItemDTO child) {
        this.children.add(child);
    }
}
