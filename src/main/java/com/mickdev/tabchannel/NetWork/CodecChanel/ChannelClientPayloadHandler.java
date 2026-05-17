package com.mickdev.tabchannel.NetWork.CodecChanel;



import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelClearMessagesPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.Render.Gui.ClientChannelNotifications;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ChannelClientPayloadHandler {

    private ChannelClientPayloadHandler() {
    }
    public static void handleClearMessages(ChannelClearMessagesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientChannelChatState.clearChannel(payload.channelId()));
    }
    public static void handleClearTabs(ChannelClearTabsPayload payload, IPayloadContext context) {
        context.enqueueWork(ClientChannelTabState::clear);
    }

    public static void handleAddTab(ChannelAddTabPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientChannelTabState.addTab(
                payload.id(),
                payload.displayName(),
                payload.global(),
                payload.selected(),
                payload.page(),
                payload.tabColor(),
                payload.staffChannel()
        ));
    }

    public static void handleChannelMessage(ChannelMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientChannelChatState.push(
                payload.channelId(),
                payload.message()
        ));
    }

    public static void handleMentionNotify(ChannelMentionNotifyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientChannelNotifications.onMentionNotify(
                payload.channelId(),
                payload.preview()
        ));
    }
}