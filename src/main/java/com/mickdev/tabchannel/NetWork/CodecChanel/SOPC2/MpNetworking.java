package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class MpNetworking {

    private MpNetworking() {
    }

    public static void syncMessage(ServerPlayer player, String peerName, String message, boolean incoming) {
        if (player == null || peerName == null || peerName.isBlank() || message == null) {
            return;
        }

        if (ServerPlayNetworking.canSend(player, MpMessagePayload.TYPE)) {
            ServerPlayNetworking.send(player, new MpMessagePayload(peerName, message, incoming));
        }
    }
}
