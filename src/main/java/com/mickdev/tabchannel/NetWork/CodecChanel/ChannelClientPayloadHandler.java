package com.mickdev.tabchannel.NetWork.CodecChanel;

import com.mickdev.tabchannel.Common.Mp.ClientMpChatStore;
import com.mickdev.tabchannel.Common.Mp.ClientMpNotifications;
import com.mickdev.tabchannel.Common.Mp.ClientMpAccess;
import com.mickdev.tabchannel.Common.Mp.ClientMpPersistence;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpBrowseModePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelClearMessagesPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpMessagePayload;
import com.mickdev.tabchannel.Render.Gui.ClientChannelNotifications;

public final class ChannelClientPayloadHandler {
    private ChannelClientPayloadHandler() {}

    public static void handleClearMessages(ChannelClearMessagesPayload payload) {
        ClientChannelChatState.clearChannel(payload.channelId());
    }

    public static void handleClearTabs(ChannelClearTabsPayload payload) {
        ClientChannelTabState.clear();
    }

    public static void handleAddTab(ChannelAddTabPayload payload) {
        ClientChannelTabState.addTab(payload.id(), payload.displayName(), payload.global(), payload.selected(), payload.page(), payload.tabColor(), payload.staffChannel());
    }

    public static void handleChannelMessage(ChannelMessagePayload payload) {
        ClientChannelChatState.push(payload.channelId(), payload.message());
    }

    public static void handleMentionNotify(ChannelMentionNotifyPayload payload) {
        ClientChannelNotifications.onMentionNotify(payload.channelId(), payload.preview());
    }

    public static void handleMpBrowseMode(MpBrowseModePayload payload) {
        ClientMpAccess.setBrowseAllOnline(payload.browseAllOnline());
    }

    public static void handleMpMessage(MpMessagePayload payload) {
        ClientMpChatStore.addMessage(payload.peerName(), payload.message(), payload.incoming());

        if (payload.incoming()) {
            ClientMpNotifications.onIncoming(payload.peerName());
        }

        ClientMpPersistence.saveForCurrentServer();
    }
}
