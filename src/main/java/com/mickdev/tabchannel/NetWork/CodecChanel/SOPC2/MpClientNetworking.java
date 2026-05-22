package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import net.neoforged.neoforge.network.PacketDistributor;

public final class MpClientNetworking {

    private MpClientNetworking() {
    }

    public static void send(String targetName, String message) {
        if (targetName == null || targetName.isBlank() || message == null || message.isBlank()) {
            return;
        }

        PacketDistributor.sendToServer(new MpSendPayload(targetName, message));
    }
}
