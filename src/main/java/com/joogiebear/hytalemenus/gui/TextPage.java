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
import com.joogiebear.hytalemenus.config.PageConfig;
import com.joogiebear.hytalemenus.util.MessageUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom UI page that displays text content.
 * Used for rules, info pages, announcements, etc.
 */
public class TextPage extends InteractiveCustomUIPage<TextPage.TextEventData> {

    private final PageConfig pageConfig;
    private final String pageName;
    private final String parentMenu;

    /**
     * Event data received when back button is clicked.
     */
    public static class TextEventData {
        public String action;

        public static final BuilderCodec<TextEventData> CODEC = BuilderCodec
                .builder(TextEventData.class, TextEventData::new)
                .append(
                        new KeyedCodec<>("Action", Codec.STRING),
                        (TextEventData o, String v) -> o.action = v,
                        (TextEventData o) -> o.action
                )
                .add()
                .build();
    }

    public TextPage(@Nonnull PlayerRef playerRef, PageConfig pageConfig, String pageName, @Nullable String parentMenu) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TextEventData.CODEC);
        this.pageConfig = pageConfig;
        this.pageName = pageName;
        this.parentMenu = parentMenu;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/TextPage.ui");

        String playerName = playerRef.getUsername();

        // Set page title
        String title = MessageUtil.replacePlaceholders(
            MessageUtil.translateColors(pageConfig.getTitle()),
            playerName
        );
        cmd.set("#Title.Text", title);

        // Set content text - join all lines with newlines
        StringBuilder content = new StringBuilder();
        for (String line : pageConfig.getLines()) {
            String processedLine = MessageUtil.replacePlaceholders(
                MessageUtil.translateColors(line),
                playerName
            );
            content.append(processedLine).append("\n");
        }
        cmd.set("#Content.Text", content.toString().trim());

        // Bind back button
        evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                new EventData().append("Action", "back")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull TextEventData data
    ) {
        if ("back".equals(data.action)) {
            Player player = store.getComponent(ref, Player.getComponentType());

            // Go back to parent menu if specified, otherwise just close
            if (parentMenu != null && !parentMenu.isEmpty()) {
                HytaleMenusPlugin plugin = HytaleMenusPlugin.getInstance();
                MenuConfig menuConfig = plugin.getConfigManager().getMenu(parentMenu);
                if (menuConfig != null) {
                    MenuPage page = new MenuPage(playerRef, plugin, menuConfig, parentMenu);
                    player.getPageManager().openCustomPage(ref, store, page);
                    return;
                }
            }

            // Fallback: just close
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }
}
