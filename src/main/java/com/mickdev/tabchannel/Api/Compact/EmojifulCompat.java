package com.mickdev.tabchannel.Api.Compact;

import net.neoforged.fml.ModList;

import net.neoforged.fml.ModList;

public final class EmojifulCompat {

    private EmojifulCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("emojiful");
    }

    public static String formatMessage(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        // Emojiful installé
        if (isLoaded()) {
            return text;
        }

        // fallback simple
        text = text.replace(":smile:", "😊");
        text = text.replace(":heart:", "❤");
        text = text.replace(":fire:", "🔥");
        text = text.replace(":skull:", "💀");
        text = text.replace(":sob:", "😭");
        text = text.replace(":joy:", "😂");
        text = text.replace(":thumbsup:", "👍");
        text = text.replace(":check:", "✔");
        text = text.replace(":x:", "❌");

        return text;
    }
}