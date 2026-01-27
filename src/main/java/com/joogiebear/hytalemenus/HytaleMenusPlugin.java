package com.joogiebear.hytalemenus;

import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.joogiebear.hytalemenus.api.ActionRegistry;
import com.joogiebear.hytalemenus.commands.MenuCommand;
import com.joogiebear.hytalemenus.commands.MenuAdminCommand;
import com.joogiebear.hytalemenus.config.ConfigManager;
import com.joogiebear.hytalemenus.config.MenuConfig;
import com.joogiebear.hytalemenus.config.PageConfig;
import com.joogiebear.hytalemenus.gui.MenuPage;
import com.joogiebear.hytalemenus.gui.TextPage;
import com.joogiebear.hytalemenus.util.MessageUtil;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * HytaleMenus - Custom GUI menu plugin for Hytale servers.
 * Allows server owners to create configurable menus with nested sub-menus.
 *
 * Other plugins can register custom actions using the API:
 * <pre>
 * HytaleMenusAPI api = HytaleMenusAPI.getInstance();
 * api.registerAction("myplugin:myaction", (player, playerRef, ref, store, args) -> {
 *     // Handle the action
 * });
 * </pre>
 */
public class HytaleMenusPlugin extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("HytaleMenus");
    private static HytaleMenusPlugin instance;

    private ConfigManager configManager;
    private ActionRegistry actionRegistry;

    public HytaleMenusPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        LOGGER.info("HytaleMenus is setting up...");

        // Initialize action registry
        actionRegistry = new ActionRegistry();

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Register built-in actions
        registerBuiltInActions();
    }

    @Override
    protected void start() {
        LOGGER.info("HytaleMenus is starting...");

        // Register commands
        getCommandRegistry().registerCommand(new MenuCommand(this));
        LOGGER.info("Registered /menu command");

        getCommandRegistry().registerCommand(new MenuAdminCommand(this));
        LOGGER.info("Registered /menuadmin command");

        LOGGER.info("HytaleMenus has been enabled! Loaded " + configManager.getMenuCount() + " menus.");
        LOGGER.info("Registered " + actionRegistry.getRegisteredActions().size() + " actions.");
    }

    @Override
    protected void shutdown() {
        LOGGER.info("HytaleMenus has been disabled!");
    }

    /**
     * Register built-in actions.
     */
    private void registerBuiltInActions() {
        // close - Close the current menu
        actionRegistry.register("close", (player, playerRef, ref, store, args) -> {
            player.getPageManager().setPage(ref, store, Page.None);
        });

        // menu - Open another menu
        // Usage: menu:menuname
        actionRegistry.register("menu", (player, playerRef, ref, store, args) -> {
            if (args.length < 1) {
                playerRef.sendMessage(MessageUtil.error("Menu name required."));
                return;
            }

            String menuName = args[0];
            MenuConfig menuConfig = configManager.getMenu(menuName);

            if (menuConfig == null) {
                playerRef.sendMessage(MessageUtil.error("Menu not found: " + menuName));
                return;
            }

            // Check permission
            if (menuConfig.hasPermission() && !player.hasPermission(menuConfig.getPermission(), false)) {
                playerRef.sendMessage(MessageUtil.of(configManager.getNoPermissionMessage()));
                return;
            }

            MenuPage page = new MenuPage(playerRef, this, menuConfig, menuName);
            player.getPageManager().openCustomPage(ref, store, page);
        });

        // page - Open a text content page
        // Usage: page:rules or page:rules:parentmenu
        actionRegistry.register("page", (player, playerRef, ref, store, args) -> {
            if (args.length < 1) {
                playerRef.sendMessage(MessageUtil.error("Page name required."));
                return;
            }

            String pageName = args[0];
            String parentMenu = args.length > 1 ? args[1] : null;

            PageConfig pageConfig = configManager.getPage(pageName);

            if (pageConfig == null) {
                playerRef.sendMessage(MessageUtil.error("Page not found: " + pageName));
                return;
            }

            TextPage page = new TextPage(playerRef, pageConfig, pageName, parentMenu);
            player.getPageManager().openCustomPage(ref, store, page);
        });

        // message - Send a message to the player
        // Usage: message:Hello World
        actionRegistry.register("message", (player, playerRef, ref, store, args) -> {
            if (args.length < 1) return;
            String message = String.join(":", args); // Rejoin in case message had colons
            playerRef.sendMessage(MessageUtil.of(message));
        });

        // command - Run a command as the player
        // Usage: command:vault 1
        actionRegistry.register("command", (player, playerRef, ref, store, args) -> {
            if (args.length < 1) return;
            String command = String.join(" ", args);
            if (!command.startsWith("/")) {
                command = "/" + command;
            }
            CommandManager.get().handleCommand(playerRef, command);
        });

        LOGGER.info("Registered built-in actions: close, menu, page, message, command");
    }

    /**
     * Get the plugin instance.
     */
    public static HytaleMenusPlugin getInstance() {
        return instance;
    }

    /**
     * Get the configuration manager.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the action registry.
     */
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    /**
     * Get the plugin's data directory path.
     */
    public Path getPluginDataPath() {
        return Path.of("mods", "HytaleMenus");
    }

    /**
     * Reload the plugin configuration.
     */
    public void reload() {
        configManager.loadConfig();
        LOGGER.info("Configuration reloaded. Loaded " + configManager.getMenuCount() + " menus and " + configManager.getPageCount() + " pages.");
    }
}
