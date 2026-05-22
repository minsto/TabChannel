package com.mickdev.tabchannel.NetWork.CodecChanel;

import com.mickdev.tabchannel.Common.Mp.MpAccess;
import com.mickdev.tabchannel.Common.Mp.MpContactRegistry;
import com.mickdev.tabchannel.Common.Mp.PrivateMessageService;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpSendPayload;
import net.minecraft.server.level.ServerPlayer;

public final class MpServerPayloadHandler {

    private MpServerPayloadHandler() {
    }

    public static void handleSend(MpSendPayload payload, ServerPlayer sender) {
        if (sender == null || payload == null) {
            return;
        }

        String targetName = payload.targetName();
        String message = payload.message();

        if (targetName == null || targetName.isBlank() || message == null || message.isBlank()) {
            return;
        }

        if (!MpAccess.canBrowseAllOnlinePlayers(sender)
                && !MpContactRegistry.hasContactByName(sender, targetName)) {
            PrivateMessageService.notifyNeedContact(sender);
            return;
        }

        PrivateMessageService.sendOrQueue(sender, targetName, message);
    }
}
