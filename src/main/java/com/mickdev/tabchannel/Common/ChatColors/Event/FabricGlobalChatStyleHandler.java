package com.mickdev.tabchannel.Common.ChatColors.Event;

import com.mickdev.tabchannel.TabChannel;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.level.ServerPlayer;

public final class FabricGlobalChatStyleHandler {

    private FabricGlobalChatStyleHandler() {
    }

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (!(sender instanceof ServerPlayer player)) {
                return true;
            }

            try {
                return FabricServerChatRouter.allowChatMessage(message, player);
            } catch (RuntimeException ex) {
                TabChannel.LOGGER.error("Failed to handle global chat for {}", sender.getGameProfile().getName(), ex);
                return true;
            }
        });
    }
}
