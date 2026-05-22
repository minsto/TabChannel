package com.mickdev.tabchannel.Common.ChatColors.Events;

import com.mickdev.tabchannel.Common.ChatColors.ChatStyleFormatter;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = TabChannel.MODID)
public final class NeoForgeGlobalChatStyleHandler {

    private NeoForgeGlobalChatStyleHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (player == null || player.server == null) {
            return;
        }

        String rawText = event.getRawText();

        if (rawText == null || rawText.isBlank()) {
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);

        ChatLogStorage.log(
                player.server,
                player,
                "global",
                rawText
        );

        Component message = Component.literal(player.getGameProfile().getName() + ": ")
                .withStyle(ChatFormatting.WHITE)
                .append(ChatStyleFormatter.format(player, rawText));

        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }
}