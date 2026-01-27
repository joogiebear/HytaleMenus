package com.joogiebear.hytalemenus.util;

import com.hypixel.hytale.server.core.Message;

/**
 * Utility class for creating Hytale Message objects.
 */
public class MessageUtil {

    private MessageUtil() {}

    /**
     * Create a Message from a plain string.
     */
    public static Message of(String text) {
        return Message.raw(translateColors(text));
    }

    /**
     * Create a colored message.
     */
    public static Message colored(String text, String color) {
        return Message.raw(translateColors(text)).color(color);
    }

    /**
     * Create an error message (red).
     */
    public static Message error(String text) {
        return Message.raw(translateColors(text)).color("#FF5555");
    }

    /**
     * Create a success message (green).
     */
    public static Message success(String text) {
        return Message.raw(translateColors(text)).color("#55FF55");
    }

    /**
     * Create an info message (yellow).
     */
    public static Message info(String text) {
        return Message.raw(translateColors(text)).color("#FFFF55");
    }

    /**
     * Translate color codes (&) to hex colors for display.
     * Note: In Hytale, we use Message.color() for actual coloring,
     * but this strips the & codes from text.
     */
    public static String translateColors(String text) {
        if (text == null) return "";

        // Strip color codes for now - Hytale uses Message.color() instead
        return text
            .replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * Replace placeholders in text.
     */
    public static String replacePlaceholders(String text, String playerName) {
        if (text == null) return "";

        return text
            .replace("{player}", playerName)
            .replace("{PLAYER}", playerName)
            .replace("%player%", playerName);
    }
}
