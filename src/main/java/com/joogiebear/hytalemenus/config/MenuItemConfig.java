package com.joogiebear.hytalemenus.config;

import java.util.List;

/**
 * Configuration for a single menu item.
 */
public class MenuItemConfig {

    private String icon;
    private String name;
    private List<String> lore;
    private String action;
    private String permission;
    private String noPermission = "deny"; // "deny" or "hide"
    private Integer closeCommandDelayMs; // null = use global default

    public MenuItemConfig() {}

    public MenuItemConfig(String icon, String name, String action) {
        this.icon = icon;
        this.name = name;
        this.action = action;
    }

    /**
     * Get the item icon (item type name).
     */
    public String getIcon() {
        return icon != null ? icon : "STONE";
    }

    /**
     * Get the display name.
     */
    public String getName() {
        return name != null ? name : "Menu Item";
    }

    /**
     * Get the lore lines.
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * Get the action to perform on click.
     * Formats: "command:<cmd>", "console:<cmd>", "menu:<name>", "close"
     */
    public String getAction() {
        return action;
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
     * Check if item should be hidden when player lacks permission.
     */
    public boolean shouldHideWithoutPermission() {
        return "hide".equalsIgnoreCase(getNoPermission());
    }

    // Setters for deserialization
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setNoPermission(String noPermission) {
        this.noPermission = noPermission;
    }

    public Integer getCloseCommandDelayMs() {
        return closeCommandDelayMs;
    }

    public void setCloseCommandDelayMs(Integer closeCommandDelayMs) {
        this.closeCommandDelayMs = closeCommandDelayMs;
    }

    public boolean hasCloseCommandDelay() {
        return closeCommandDelayMs != null;
    }
}
