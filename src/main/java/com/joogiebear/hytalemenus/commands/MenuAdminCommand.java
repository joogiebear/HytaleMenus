package com.joogiebear.hytalemenus.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.joogiebear.hytalemenus.HytaleMenusPlugin;
import com.joogiebear.hytalemenus.util.MessageUtil;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Admin command for menu management.
 * Usage: /menuadmin <subcommand>
 */
public class MenuAdminCommand extends AbstractCommand {

    private final HytaleMenusPlugin plugin;

    public MenuAdminCommand(HytaleMenusPlugin plugin) {
        super("menuadmin", "Admin commands for menu management");
        this.plugin = plugin;

        // Set permission
        requirePermission("hytalemenus.admin");

        // Add subcommands
        addSubCommand(new ReloadSubCommand(plugin));
        addSubCommand(new ListSubCommand(plugin));
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        ctx.sendMessage(MessageUtil.info("HytaleMenus Admin Commands:"));
        ctx.sendMessage(MessageUtil.of("/menuadmin reload - Reload configuration"));
        ctx.sendMessage(MessageUtil.of("/menuadmin list - List all menus"));
        return CompletableFuture.completedFuture(null);
    }

    // Subcommand: reload
    private static class ReloadSubCommand extends AbstractCommand {
        private final HytaleMenusPlugin plugin;

        public ReloadSubCommand(HytaleMenusPlugin plugin) {
            super("reload", "Reload configuration");
            this.plugin = plugin;
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            plugin.reload();
            ctx.sendMessage(MessageUtil.success("Configuration reloaded. Loaded " +
                plugin.getConfigManager().getMenuCount() + " menus."));
            return CompletableFuture.completedFuture(null);
        }
    }

    // Subcommand: list
    private static class ListSubCommand extends AbstractCommand {
        private final HytaleMenusPlugin plugin;

        public ListSubCommand(HytaleMenusPlugin plugin) {
            super("list", "List all menus");
            this.plugin = plugin;
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            Set<String> menus = plugin.getConfigManager().getMenuNames();

            if (menus.isEmpty()) {
                ctx.sendMessage(MessageUtil.info("No menus configured."));
                return CompletableFuture.completedFuture(null);
            }

            ctx.sendMessage(MessageUtil.info("Available menus (" + menus.size() + "):"));
            for (String menu : menus) {
                String isDefault = menu.equals(plugin.getConfigManager().getDefaultMenu()) ? " (default)" : "";
                ctx.sendMessage(MessageUtil.of("  " + menu + isDefault));
            }

            return CompletableFuture.completedFuture(null);
        }
    }
}
