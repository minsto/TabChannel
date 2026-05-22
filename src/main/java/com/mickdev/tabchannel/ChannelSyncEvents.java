package com.mickdev.tabchannel;

import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.Common.Mp.MpOfflineMailbox;
import com.mickdev.tabchannel.mention.ChannelMentionAntiSpam;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class ChannelSyncEvents {
    private ChannelSyncEvents() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ChatManager.resetForNewServer();
            ChatLogStorage.clearCache();
            ChannelMentionAntiSpam.clearAll();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.player));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sync(newPlayer));
    }

    private static void sync(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ChannelSavedData.get(player.serverLevel());
        ChatManager.rebuildPlayerTabs(player.getUUID());
        ChannelSyncService.syncPlayer(player);
        MpOfflineMailbox.deliverPending(player);
    }
}
