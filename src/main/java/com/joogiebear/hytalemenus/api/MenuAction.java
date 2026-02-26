package com.joogiebear.hytalemenus.api;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Interface for menu actions that can be triggered from menu items.
 *
 * Other plugins can implement this interface and register their actions
 * with the ActionRegistry to make them available in menus.
 *
 * Example implementation:
 * <pre>
 * public class OpenVaultAction implements MenuAction {
 *     @Override
 *     public void execute(Player player, PlayerRef playerRef,
 *                         Ref<EntityStore> ref, Store<EntityStore> store,
 *                         String[] args) {
 *         // Open the vault UI
 *         int vaultNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;
 *         vaultUI.openVault(player, ref, store, playerRef, vault, vaultNumber);
 *     }
 * }
 * </pre>
 */
public interface MenuAction {

    /**
     * Execute this action for the given player.
     *
     * @param player    The player entity
     * @param playerRef The player reference for sending messages
     * @param ref       Entity store reference (for UI operations)
     * @param store     Entity store (for UI operations)
     * @param args      Additional arguments passed from the action string
     */
    void execute(Player player, PlayerRef playerRef,
                 Ref<EntityStore> ref, Store<EntityStore> store,
                 String[] args);
}
