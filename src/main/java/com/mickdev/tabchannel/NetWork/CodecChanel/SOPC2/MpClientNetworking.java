package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class MpClientNetworking {

    private MpClientNetworking() {
    }

    public static void send(String targetName, String message) {
        if (targetName == null || targetName.isBlank() || message == null || message.isBlank()) {
            return;
        }

        ClientPlayNetworking.send(new MpSendPayload(targetName, message));
    }
}
