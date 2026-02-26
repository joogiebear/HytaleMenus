package com.joogiebear.hytalemenus.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.joogiebear.hytalemenus.HytaleMenusPlugin;
import com.joogiebear.hytalemenus.config.MenuConfig;
import com.joogiebear.hytalemenus.gui.MenuPage;

/**
 * Public API for HytaleMenus.
 *
 * Use this class to:
 * - Register custom actions that can be triggered from menu items
 * - Open menus programmatically
 *
 * Example usage:
 * <pre>
 * // Get the API
 * HytaleMenusAPI api = HytaleMenusAPI.getInstance();
 *
 * // Register an action
 * api.registerAction("myplugin:openui", (player, playerRef, ref, store, args) -> {
 *     // Open your custom UI here
 *     MyCustomPage page = new MyCustomPage(playerRef);
 *     player.getPageManager().openCustomPage(ref, store, page);
 * });
 *
 * // Now in menu config, use:
 * // "action": "myplugin:openui"
 * </pre>
 */
public class HytaleMenusAPI {

    private static HytaleMenusAPI instance;

    private HytaleMenusAPI() {}

    /**
     * Get the API instance.
     *
     * @throws IllegalStateException if HytaleMenus is not loaded
     */
    public static HytaleMenusAPI getInstance() {
        if (instance == null) {
            if (HytaleMenusPlugin.getInstance() == null) {
                throw new IllegalStateException("HytaleMenus is not loaded!");
            }
            instance = new HytaleMenusAPI();
        }
        return instance;
    }

    /**
     * Get the action registry for registering custom actions.
     *
     * @return The action registry
     */
    public ActionRegistry getActionRegistry() {
        return HytaleMenusPlugin.getInstance().getActionRegistry();
    }

    /**
     * Register a custom action.
     *
     * Shortcut for getActionRegistry().register(name, action).
     *
     * @param name   The action name (e.g., "myplugin:myaction")
     * @param action The action implementation
     */
    public void registerAction(String name, MenuAction action) {
        getActionRegistry().register(name, action);
    }

    /**
     * Open a menu for a player.
     *
     * @param player    The player
     * @param playerRef The player reference
     * @param ref       Entity store reference
     * @param store     Entity store
     * @param menuName  The menu name to open
     * @return true if menu was opened, false if menu not found
     */
    public boolean openMenu(Player player, PlayerRef playerRef,
                           Ref<EntityStore> ref, Store<EntityStore> store,
                           String menuName) {
        HytaleMenusPlugin plugin = HytaleMenusPlugin.getInstance();
        MenuConfig menuConfig = plugin.getConfigManager().getMenu(menuName);

        if (menuConfig == null) {
            return false;
        }

        MenuPage page = new MenuPage(playerRef, plugin, menuConfig, menuName);
        player.getPageManager().openCustomPage(ref, store, page);
        return true;
    }
}
