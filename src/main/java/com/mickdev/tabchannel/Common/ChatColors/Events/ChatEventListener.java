package com.mickdev.tabchannel.Common.ChatColors.Events;

import com.mickdev.tabchannel.Common.ChatColors.ChatStyleFormatter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

public class ChatEventListener {

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String rawText = event.getRawText();

        // Appliquer ton formateur
        Component formattedMessage = ChatStyleFormatter.format(player, rawText);

        // Remplacer le message original
        event.setMessage(formattedMessage);
    }
}
