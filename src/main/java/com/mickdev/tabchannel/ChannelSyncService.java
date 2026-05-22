package com.mickdev.tabchannel;


import com.mickdev.tabchannel.Common.Mp.MpAccess;
import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelAddTabPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelClearTabsPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpBrowseModePayload;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ChannelSyncService {

    private ChannelSyncService() {
    }

    public static void syncPlayer(ServerPlayer player) {
        ChannelSavedData.get(player.serverLevel());
        ChatManager.rebuildPlayerTabs(player.getUUID());

        ChatTabState state = ChatManager.getState(player.getUUID());

        ServerPlayNetworking.send(player, new ChannelClearTabsPayload());
        ServerPlayNetworking.send(player, new MpBrowseModePayload(MpAccess.canBrowseAllOnlinePlayers(player)));

        for (String id : state.getOpenedTabs()) {
            ChatChannel channel = ChatManager.getChannel(id);
            if (channel == null) {
                continue;
            }

            ServerPlayNetworking.send(player, new ChannelAddTabPayload(
                    channel.getId(),
                    channel.getDisplayName(),
                    channel.isOriginalGlobal(),
                    channel.getId().equals(state.getSelectedChannelId()),
                    state.getPage(),
                    channel.getTabColor(),
                    channel.isStaffChannel()
            ));
        }
    }
}
