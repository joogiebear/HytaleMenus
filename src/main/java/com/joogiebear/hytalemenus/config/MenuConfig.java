package com.joogiebear.hytalemenus.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a single menu.
 */
public class MenuConfig {

    private String title;
    private int rows = 3;
    private String permission;
    private String noPermission = "deny";
    private Map<String, MenuItemConfig> items = new HashMap<>();

    public MenuConfig() {}

    public MenuConfig(String title, int rows) {
        this.title = title;
        this.rows = rows;
    }

    /**
     * Get the menu title.
     */
    public String getTitle() {
        return title != null ? title : "Menu";
    }

    /**
     * Get the number of rows (1-6).
     */
    public int getRows() {
        return Math.max(1, Math.min(6, rows));
    }

    /**
     * Get the total number of slots.
     */
    public int getSize() {
        return getRows() * 9;
    }

    /**
     * Get the required permission (optional).
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Check if a permission is required.
     */
    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    /**
     * Get the no-permission behavior: "deny" or "hide".
     */
    public String getNoPermission() {
        return noPermission != null ? noPermission : "deny";
    }

    /**
     * Get all menu items. Keys are slot numbers as strings.
     */
    public Map<String, MenuItemConfig> getItems() {
        return items != null ? items : new HashMap<>();
    }

    /**
     * Get a specific item by slot.
     */
    public MenuItemConfig getItem(int slot) {
        return items != null ? items.get(String.valueOf(slot)) : null;
    }

    // Setters for deserialization
    public void setTitle(String title) {
        this.title = title;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setNoPermission(String noPermission) {
        this.noPermission = noPermission;
    }

    public void setItems(Map<String, MenuItemConfig> items) {
        this.items = items;
    }
}
