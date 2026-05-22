package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MpNetworking {

    private MpNetworking() {
    }

    public static void syncMessage(ServerPlayer player, String peerName, String message, boolean incoming) {
        if (player == null || peerName == null || peerName.isBlank() || message == null) {
            return;
        }

        PacketDistributor.sendToPlayer(player, new MpMessagePayload(peerName, message, incoming));
    }
}
