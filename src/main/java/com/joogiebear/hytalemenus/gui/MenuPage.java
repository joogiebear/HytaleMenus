package com.joogiebear.hytalemenus.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.joogiebear.hytalemenus.HytaleMenusPlugin;
import com.joogiebear.hytalemenus.config.MenuConfig;
import com.joogiebear.hytalemenus.config.MenuItemConfig;
import com.joogiebear.hytalemenus.util.MessageUtil;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Custom UI page that renders a configurable menu.
 * Uses a 3x3 grid layout (9 slots) with dynamic button text and visibility.
 */
public class MenuPage extends InteractiveCustomUIPage<MenuPage.MenuEventData> {

    private static final Logger LOGGER = Logger.getLogger("HytaleMenus");
    private static final int MAX_SLOTS = 9;

    private final HytaleMenusPlugin plugin;
    private final MenuConfig menuConfig;
    private final String menuName;

    /**
     * Event data received when a button is clicked.
     */
    public static class MenuEventData {
        public String action;

        public static final BuilderCodec<MenuEventData> CODEC = BuilderCodec
                .builder(MenuEventData.class, MenuEventData::new)
                .append(
                        new KeyedCodec<>("Action", Codec.STRING),
                        (MenuEventData o, String v) -> o.action = v,
                        (MenuEventData o) -> o.action
                )
                .add()
                .build();
    }

    public MenuPage(@Nonnull PlayerRef playerRef, HytaleMenusPlugin plugin,
                    MenuConfig menuConfig, String menuName) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, MenuEventData.CODEC);
        this.plugin = plugin;
        this.menuConfig = menuConfig;
        this.menuName = menuName;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/MenuGrid.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        String playerName = playerRef.getUsername();

        // Set menu title
        String title = MessageUtil.replacePlaceholders(
            MessageUtil.translateColors(menuConfig.getTitle()),
            playerName
        );
        cmd.set("#Title.Text", title);

        // Configure each slot
        Map<String, MenuItemConfig> items = menuConfig.getItems();

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            String buttonId = "#Slot" + slot;
            MenuItemConfig itemConfig = items.get(String.valueOf(slot));

            if (itemConfig == null) {
                // Empty slot - keep hidden
                continue;
            }

            // Check permission - hide if needed
            if (itemConfig.hasPermission() && itemConfig.shouldHideWithoutPermission()) {
                if (!hasPermission(player, itemConfig.getPermission())) {
                    continue; // Skip this item, leave hidden
                }
            }

            // Set button text
            String itemName = MessageUtil.replacePlaceholders(
                MessageUtil.translateColors(itemConfig.getName()),
                playerName
            );
            cmd.set("#Slot" + slot + "Text.Text", itemName);

            // Set icon using ItemId property
            String iconId = itemConfig.getIcon();
            if (iconId != null && !iconId.isEmpty()) {
                cmd.set("#Slot" + slot + "Icon.ItemId", iconId);
            }

            // Bind click event
            evt.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    buttonId,
                    new EventData().append("Action", String.valueOf(slot))
            );
        }

        // Bind close button
        evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                new EventData().append("Action", "close")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull MenuEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("close".equals(data.action)) {
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        // Parse slot number
        int slot;
        try {
            slot = Integer.parseInt(data.action);
        } catch (NumberFormatException e) {
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        // Get item config
        MenuItemConfig itemConfig = menuConfig.getItem(slot);
        if (itemConfig == null) {
            return;
        }

        // Check permission
        if (itemConfig.hasPermission() && !hasPermission(player, itemConfig.getPermission())) {
            playerRef.sendMessage(MessageUtil.of(plugin.getConfigManager().getNoPermissionMessage()));
            return;
        }

        // Execute action
        String action = itemConfig.getAction();
        if (action == null || action.isEmpty()) {
            return;
        }

        executeAction(action, itemConfig, player, ref, store);
    }

    /**
     * Execute a menu item action using the action registry.
     *
     * Action format: "actionname:arg1:arg2:..."
     *
     * Built-in actions:
     * - close           : Close the menu
     * - menu:menuname   : Open another menu
     * - message:text    : Send a message to the player
     *
     * Plugins can register custom actions like:
     * - vault:open:1    : Open vault #1 (if HytaleVault registers this)
     * - shop:browse     : Open shop browser
     */
    private void executeAction(String action, com.joogiebear.hytalemenus.config.MenuItemConfig itemConfig,
                               Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        String playerName = playerRef.getUsername();

        // Replace placeholders in the action string
        action = MessageUtil.replacePlaceholders(action, playerName);

        // Special handling for close-command and close-console — supports configurable delays
        if (action.startsWith("close-command:") || action.startsWith("close-console:")) {
            boolean isConsole = action.startsWith("close-console:");
            String command = action.substring(isConsole ? "close-console:".length() : "close-command:".length());
            if (command.startsWith("/")) command = command.substring(1);
            if (isConsole) command = command.replace("%player%", playerName);

            // Resolve delay: item override → global config → 100ms fallback
            long delayMs = itemConfig != null && itemConfig.hasCloseCommandDelay()
                ? itemConfig.getCloseCommandDelayMs()
                : plugin.getConfigManager().getCloseCommandDelayMs();

            player.getPageManager().setPage(ref, store, com.hypixel.hytale.protocol.packets.interface_.Page.None);

            final String finalCommand = command;
            final boolean finalIsConsole = isConsole;
            java.util.concurrent.CompletableFuture.delayedExecutor(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (finalIsConsole) {
                        com.hypixel.hytale.server.core.command.system.CommandManager.get()
                            .handleCommand(com.hypixel.hytale.server.core.console.ConsoleSender.INSTANCE, finalCommand);
                    } else {
                        com.hypixel.hytale.server.core.command.system.CommandManager.get()
                            .handleCommand(player, finalCommand);
                    }
                });
            return;
        }

        // Execute via action registry for all other actions
        boolean executed = plugin.getActionRegistry().execute(action, player, playerRef, ref, store);

        if (!executed) {
            playerRef.sendMessage(MessageUtil.error("Unknown action: " + action));
            LOGGER.warning("Unknown action: " + action);
        }
    }

    /**
     * Check if player has a permission.
     */
    private boolean hasPermission(Player player, String permission) {
        if (player == null) return false;
        return player.hasPermission(permission, false);
    }
}
