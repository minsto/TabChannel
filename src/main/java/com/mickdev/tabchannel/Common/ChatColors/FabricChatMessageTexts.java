package com.mickdev.tabchannel.Common.ChatColors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

public final class FabricChatMessageTexts {

    private FabricChatMessageTexts() {
    }

    /**
     * Raw text sent by the player (with {@code #color} tags), before chat decorators run.
     */
    public static String extractRaw(PlayerChatMessage message) {
        if (message == null) {
            return "";
        }

        String signed = message.signedContent();
        if (signed != null && !signed.isBlank()) {
            return signed.trim();
        }

        Component unsigned = message.unsignedContent();
        if (unsigned != null) {
            String text = unsigned.getString();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }

        Component decorated = message.decoratedContent();
        if (decorated != null) {
            String text = decorated.getString();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }

        return "";
    }
}
