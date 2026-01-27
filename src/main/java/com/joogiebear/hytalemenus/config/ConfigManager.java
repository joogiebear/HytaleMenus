package com.joogiebear.hytalemenus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.joogiebear.hytalemenus.HytaleMenusPlugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages plugin configuration and menu definitions.
 */
public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger("HytaleMenus");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final HytaleMenusPlugin plugin;
    private final Path configPath;

    private String defaultMenu = "main";
    private String noPermissionMessage = "&cYou don't have permission to access this.";
    private Map<String, MenuConfig> menus = new HashMap<>();
    private Map<String, PageConfig> pages = new HashMap<>();

    public ConfigManager(HytaleMenusPlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getPluginDataPath().resolve("config.json");
    }

    /**
     * Load configuration from file.
     */
    public void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                createDefaultConfig();
            }

            try (Reader reader = Files.newBufferedReader(configPath)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    this.defaultMenu = data.defaultMenu != null ? data.defaultMenu : "main";
                    this.noPermissionMessage = data.noPermissionMessage != null ?
                        data.noPermissionMessage : "&cYou don't have permission to access this.";
                    this.menus = data.menus != null ? data.menus : new HashMap<>();
                    this.pages = data.pages != null ? data.pages : new HashMap<>();
                }
            }

            LOGGER.info("Loaded " + menus.size() + " menus and " + pages.size() + " pages from config.");

        } catch (IOException e) {
            LOGGER.severe("Failed to load config: " + e.getMessage());
        }
    }

    /**
     * Create default configuration file with comprehensive examples.
     */
    private void createDefaultConfig() throws IOException {
        ConfigData data = new ConfigData();

        // Documentation comments
        data._comment = java.util.List.of(
            "=== HytaleMenus Configuration ===",
            "",
            "SLOT LAYOUT (3x3 grid, slots 0-8):",
            "  [0] [1] [2]",
            "  [3] [4] [5]",
            "  [6] [7] [8]",
            "",
            "ACTIONS - Format: 'actionname' or 'actionname:arg1:arg2'",
            "",
            "Built-in actions:",
            "  close              - Close the menu",
            "  menu:menuname      - Open another menu",
            "  page:name          - Open a text page (closes on back)",
            "  page:name:menu     - Open a text page (back returns to menu)",
            "  message:text       - Send a message to the player",
            "  command:cmd args   - Run a command as the player",
            "",
            "Command examples:",
            "  command:vault        - Opens vault selector",
            "  command:vault 1      - Opens vault #1 directly",
            "  command:spawn        - Teleport to spawn",
            "  command:help         - Show help",
            "",
            "PAGES - Text content pages (rules, info, etc.):",
            "  Define pages in the 'pages' section with title and lines.",
            "  Open with 'page:pagename' action.",
            "",
            "PERMISSIONS:",
            "  permission: 'node'     - Required permission to see/use item",
            "  noPermission: 'deny'   - Show error when clicked without permission",
            "  noPermission: 'hide'   - Hide item completely without permission",
            "",
            "PLACEHOLDERS:",
            "  {player} or %player% - Player's name"
        );

        data.defaultMenu = "main";
        data.noPermissionMessage = "You don't have permission to access this.";
        data.menus = new HashMap<>();

        // ===== MAIN MENU =====
        MenuConfig mainMenu = new MenuConfig();
        mainMenu.setTitle("Server Menu");
        mainMenu.setRows(3);

        Map<String, MenuItemConfig> mainItems = new HashMap<>();

        // Vaults item - runs /vault command
        MenuItemConfig vaultsItem = new MenuItemConfig();
        vaultsItem.setIcon("CHEST");
        vaultsItem.setName("Personal Vaults");
        vaultsItem.setLore(java.util.List.of(
            "Access your personal storage",
            "Click to open!"
        ));
        vaultsItem.setAction("command:vault"); // Runs /vault command
        mainItems.put("3", vaultsItem);

        // Info submenu
        MenuItemConfig infoItem = new MenuItemConfig();
        infoItem.setIcon("BOOK");
        infoItem.setName("Server Info");
        infoItem.setLore(java.util.List.of("Learn about our server"));
        infoItem.setAction("menu:info");
        mainItems.put("4", infoItem);

        // Admin panel - hidden for non-admins
        MenuItemConfig adminItem = new MenuItemConfig();
        adminItem.setIcon("COMMAND_BLOCK");
        adminItem.setName("Admin Panel");
        adminItem.setLore(java.util.List.of("Server administration"));
        adminItem.setAction("menu:admin");
        adminItem.setPermission("hytalemenus.admin");
        adminItem.setNoPermission("hide");
        mainItems.put("5", adminItem);

        mainMenu.setItems(mainItems);
        data.menus.put("main", mainMenu);

        // ===== INFO SUBMENU =====
        MenuConfig infoMenu = new MenuConfig();
        infoMenu.setTitle("Server Info");
        infoMenu.setRows(3);

        Map<String, MenuItemConfig> infoItems = new HashMap<>();

        // Welcome page - opens text page
        MenuItemConfig welcomeItem = new MenuItemConfig();
        welcomeItem.setIcon("PAPER");
        welcomeItem.setName("Welcome");
        welcomeItem.setLore(java.util.List.of("Server welcome info"));
        welcomeItem.setAction("page:welcome:info");
        infoItems.put("3", welcomeItem);

        // Rules page - opens text page
        MenuItemConfig rulesItem = new MenuItemConfig();
        rulesItem.setIcon("BOOK");
        rulesItem.setName("Server Rules");
        rulesItem.setLore(java.util.List.of("Read the server rules"));
        rulesItem.setAction("page:rules:info");
        infoItems.put("4", rulesItem);

        // Close example
        MenuItemConfig closeItem = new MenuItemConfig();
        closeItem.setIcon("BARRIER");
        closeItem.setName("Close Menu");
        closeItem.setLore(java.util.List.of("Close this menu"));
        closeItem.setAction("close");
        infoItems.put("5", closeItem);

        // Back button
        MenuItemConfig backItem = new MenuItemConfig();
        backItem.setIcon("ARROW");
        backItem.setName("Back");
        backItem.setLore(java.util.List.of("Return to main menu"));
        backItem.setAction("menu:main");
        infoItems.put("7", backItem);

        infoMenu.setItems(infoItems);
        data.menus.put("info", infoMenu);

        // ===== ADMIN SUBMENU =====
        MenuConfig adminMenu = new MenuConfig();
        adminMenu.setTitle("Admin Panel");
        adminMenu.setRows(3);
        adminMenu.setPermission("hytalemenus.admin");

        Map<String, MenuItemConfig> adminItems = new HashMap<>();

        // Reload button
        MenuItemConfig reloadItem = new MenuItemConfig();
        reloadItem.setIcon("REDSTONE");
        reloadItem.setName("Reload Menus");
        reloadItem.setLore(java.util.List.of("Reload menu configuration"));
        reloadItem.setAction("message:Use /menuadmin reload to reload configs");
        adminItems.put("4", reloadItem);

        // Back button
        MenuItemConfig adminBackItem = new MenuItemConfig();
        adminBackItem.setIcon("ARROW");
        adminBackItem.setName("Back");
        adminBackItem.setLore(java.util.List.of("Return to main menu"));
        adminBackItem.setAction("menu:main");
        adminItems.put("7", adminBackItem);

        adminMenu.setItems(adminItems);
        data.menus.put("admin", adminMenu);

        // ===== TEXT PAGES =====
        data.pages = new HashMap<>();

        // Rules page
        PageConfig rulesPage = new PageConfig();
        rulesPage.setTitle("Server Rules");
        rulesPage.setLines(java.util.List.of(
            "Welcome to our server, {player}!",
            "",
            "Please follow these rules:",
            "",
            "1. Be respectful to all players",
            "2. No griefing or stealing",
            "3. No cheating or exploits",
            "4. Keep chat family-friendly",
            "5. Listen to staff members",
            "",
            "Breaking rules may result in a ban.",
            "Have fun and enjoy your stay!"
        ));
        data.pages.put("rules", rulesPage);

        // Welcome page
        PageConfig welcomePage = new PageConfig();
        welcomePage.setTitle("Welcome!");
        welcomePage.setLines(java.util.List.of(
            "Hello {player}, welcome to our server!",
            "",
            "Here are some useful commands:",
            "",
            "/menu - Open this menu",
            "/vault - Access your personal storage",
            "/spawn - Return to spawn",
            "/help - Get help",
            "",
            "Enjoy your stay!"
        ));
        data.pages.put("welcome", welcomePage);

        // Write to file
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(data, writer);
        }

        LOGGER.info("Created default configuration file.");
    }

    /**
     * Get the default menu name.
     */
    public String getDefaultMenu() {
        return defaultMenu;
    }

    /**
     * Get a menu by name.
     */
    public MenuConfig getMenu(String name) {
        return menus.get(name);
    }

    /**
     * Check if a menu exists.
     */
    public boolean menuExists(String name) {
        return menus.containsKey(name);
    }

    /**
     * Get the number of loaded menus.
     */
    public int getMenuCount() {
        return menus.size();
    }

    /**
     * Get all menu names.
     */
    public java.util.Set<String> getMenuNames() {
        return menus.keySet();
    }

    /**
     * Get the no-permission message.
     */
    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    /**
     * Get a text page by name.
     */
    public PageConfig getPage(String name) {
        return pages.get(name);
    }

    /**
     * Check if a page exists.
     */
    public boolean pageExists(String name) {
        return pages.containsKey(name);
    }

    /**
     * Get the number of loaded pages.
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * Get all page names.
     */
    public java.util.Set<String> getPageNames() {
        return pages.keySet();
    }

    /**
     * Internal config data structure for JSON serialization.
     */
    private static class ConfigData {
        java.util.List<String> _comment; // Documentation comments
        String defaultMenu;
        String noPermissionMessage;
        Map<String, MenuConfig> menus;
        Map<String, PageConfig> pages;
    }
}
