package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;







import com.mickdev.tabchannel.ChatEntry;
import com.mickdev.tabchannel.ChatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ChannelNetworking {

    private ChannelNetworking() {
    }

    public static void sendChannelMessage(ServerPlayer target, String channelId, Component message) {
        if (target == null || channelId == null || channelId.isBlank() || message == null) {
            return;
        }

        ServerPlayNetworking.send(target, new ChannelMessagePayload(channelId, message));
    }

    public static void sendChannelHistory(ServerPlayer target, String channelId) {
        if (target == null || channelId == null || channelId.isBlank()) {
            return;
        }

        ServerPlayNetworking.send(target, new ChannelClearMessagesPayload(channelId));

        for (ChatEntry entry : ChatManager.get(channelId)) {
            ServerPlayNetworking.send(target, new ChannelMessagePayload(channelId, entry.raw()));
        }
    }
}
