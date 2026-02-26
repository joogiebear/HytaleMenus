package com.joogiebear.hytalemenus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.joogiebear.hytalemenus.HytaleMenusPlugin;
import com.joogiebear.hytalemenus.config.MenuConfig;
import com.joogiebear.hytalemenus.gui.MenuPage;
import com.joogiebear.hytalemenus.util.MessageUtil;

import java.util.concurrent.CompletableFuture;

/**
 * Command to open menus.
 * Usage: /menu [menuName]
 */
public class MenuCommand extends AbstractCommand {

    private final HytaleMenusPlugin plugin;

    public MenuCommand(HytaleMenusPlugin plugin) {
        super("menu", "Open a menu");
        this.plugin = plugin;
    }

    /**
     * Register subcommands for each menu defined in config.
     * Must be called after config is loaded.
     */
    public void registerMenuSubCommands() {
        for (String menuName : plugin.getConfigManager().getMenuNames()) {
            addSubCommand(new MenuNameSubCommand(plugin, menuName));
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return true; // Base menu command is open to all
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!(ctx.sender() instanceof Player player)) {
            ctx.sendMessage(MessageUtil.error("This command can only be used by players."));
            return CompletableFuture.completedFuture(null);
        }

        World world = player.getWorld();
        if (world == null) {
            ctx.sendMessage(MessageUtil.error("Failed to open menu."));
            return CompletableFuture.completedFuture(null);
        }

        // Open default menu
        String menuName = plugin.getConfigManager().getDefaultMenu();
        MenuConfig menuConfig = plugin.getConfigManager().getMenu(menuName);

        if (menuConfig == null) {
            ctx.sendMessage(MessageUtil.error("Default menu not found."));
            return CompletableFuture.completedFuture(null);
        }

        // Check menu permission
        if (menuConfig.hasPermission() && !player.hasPermission(menuConfig.getPermission(), false)) {
            ctx.sendMessage(MessageUtil.of(plugin.getConfigManager().getNoPermissionMessage()));
            return CompletableFuture.completedFuture(null);
        }

        // Open menu
        return CompletableFuture.runAsync(() -> {
            Ref<EntityStore> ref = player.getReference();
            Store<EntityStore> store = ref.getStore();
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if (playerRef == null) return;

            MenuPage page = new MenuPage(playerRef, plugin, menuConfig, menuName);
            player.getPageManager().openCustomPage(ref, store, page);
        }, world);
    }

    /**
     * Subcommand for opening a specific named menu.
     */
    public static class MenuNameSubCommand extends AbstractCommand {
        private final HytaleMenusPlugin plugin;
        private final String menuName;

        public MenuNameSubCommand(HytaleMenusPlugin plugin, String menuName) {
            super(menuName, "Open the " + menuName + " menu");
            this.plugin = plugin;
            this.menuName = menuName;
        }

        @Override
        public boolean hasPermission(CommandSender sender) {
            MenuConfig menuConfig = plugin.getConfigManager().getMenu(menuName);
            if (menuConfig == null) return false;

            if (!menuConfig.hasPermission()) return true;

            if (sender instanceof Player player) {
                return player.hasPermission(menuConfig.getPermission(), false);
            }
            return false;
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            if (!(ctx.sender() instanceof Player player)) {
                ctx.sendMessage(MessageUtil.error("This command can only be used by players."));
                return CompletableFuture.completedFuture(null);
            }

            MenuConfig menuConfig = plugin.getConfigManager().getMenu(menuName);
            if (menuConfig == null) {
                ctx.sendMessage(MessageUtil.error("Menu not found: " + menuName));
                return CompletableFuture.completedFuture(null);
            }

            World world = player.getWorld();
            if (world == null) {
                ctx.sendMessage(MessageUtil.error("Failed to open menu."));
                return CompletableFuture.completedFuture(null);
            }

            return CompletableFuture.runAsync(() -> {
                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

                if (playerRef == null) return;

                MenuPage page = new MenuPage(playerRef, plugin, menuConfig, menuName);
                player.getPageManager().openCustomPage(ref, store, page);
            }, world);
        }
    }
}
