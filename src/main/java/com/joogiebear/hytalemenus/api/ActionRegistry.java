package com.joogiebear.hytalemenus.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Registry for menu actions.
 *
 * Plugins can register their own actions here, making them available
 * for use in menu configurations.
 *
 * Usage from other plugins:
 * <pre>
 * // Get the registry
 * ActionRegistry registry = HytaleMenusPlugin.getInstance().getActionRegistry();
 *
 * // Register an action
 * registry.register("myplugin:myaction", (player, playerRef, ref, store, args) -> {
 *     // Do something when this action is triggered
 * });
 * </pre>
 *
 * Then in menu config:
 * <pre>
 * {
 *   "action": "myplugin:myaction:arg1:arg2"
 * }
 * </pre>
 */
public class ActionRegistry {

    private static final Logger LOGGER = Logger.getLogger("HytaleMenus");

    private final Map<String, MenuAction> actions = new HashMap<>();

    /**
     * Register an action with the given name.
     *
     * Action names should be namespaced like "pluginname:actionname" to avoid conflicts.
     *
     * @param name   The action name (e.g., "vault:open", "shop:browse")
     * @param action The action implementation
     */
    public void register(String name, MenuAction action) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null or empty");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        actions.put(name.toLowerCase(), action);
        LOGGER.info("Registered menu action: " + name);
    }

    /**
     * Unregister an action.
     *
     * @param name The action name to unregister
     * @return true if the action was removed, false if it wasn't registered
     */
    public boolean unregister(String name) {
        return actions.remove(name.toLowerCase()) != null;
    }

    /**
     * Check if an action is registered.
     *
     * @param name The action name
     * @return true if registered
     */
    public boolean isRegistered(String name) {
        return actions.containsKey(name.toLowerCase());
    }

    /**
     * Get all registered action names.
     *
     * @return Set of action names
     */
    public Set<String> getRegisteredActions() {
        return actions.keySet();
    }

    /**
     * Execute an action string.
     *
     * Action string format: "actionname" or "actionname:arg1:arg2:..."
     *
     * @param actionString The full action string from config
     * @param player       The player
     * @param playerRef    The player reference
     * @param ref          Entity store reference
     * @param store        Entity store
     * @return true if action was found and executed, false if not found
     */
    public boolean execute(String actionString, Player player, PlayerRef playerRef,
                          Ref<EntityStore> ref, Store<EntityStore> store) {
        if (actionString == null || actionString.isEmpty()) {
            return false;
        }

        // Parse action string: "actionname:arg1:arg2:..."
        String[] parts = actionString.split(":");
        String actionName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        MenuAction action = actions.get(actionName);
        if (action == null) {
            LOGGER.warning("Unknown action: " + actionName);
            return false;
        }

        try {
            action.execute(player, playerRef, ref, store, args);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Error executing action " + actionName + ": " + e.getMessage());
            return false;
        }
    }
}
