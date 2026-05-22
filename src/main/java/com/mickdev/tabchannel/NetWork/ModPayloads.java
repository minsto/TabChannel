package com.mickdev.tabchannel.NetWork;

import com.mickdev.tabchannel.ChannelChatService;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.NetWork.CodecChanel.*;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelClearMessagesPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.MpServerPayloadHandler;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpBrowseModePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpSendPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ModPayloads {

    private ModPayloads() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ChannelClearTabsPayload.TYPE, ChannelClearTabsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelAddTabPayload.TYPE, ChannelAddTabPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelClearMessagesPayload.TYPE, ChannelClearMessagesPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelMessagePayload.TYPE, ChannelMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelMentionNotifyPayload.TYPE, ChannelMentionNotifyPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MpMessagePayload.TYPE, MpMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MpBrowseModePayload.TYPE, MpBrowseModePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelLayoutPayload.TYPE, ChannelLayoutPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ChannelSelectTabPayload.TYPE, ChannelSelectTabPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MpSendPayload.TYPE, MpSendPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ChannelChangePagePayload.TYPE, ChannelChangePagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ChannelSendMessagePayload.TYPE, ChannelSendMessagePayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ChannelSelectTabPayload.TYPE, (payload, context) ->
                context.server().execute(() ->
                        ChannelServerPayloadHandler.handleSelectTab(payload, context.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(ChannelChangePagePayload.TYPE, (payload, context) ->
                context.server().execute(() ->
                        ChannelServerPayloadHandler.handleChangePage(payload, context.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(MpSendPayload.TYPE, (payload, context) ->
                context.server().execute(() ->
                        MpServerPayloadHandler.handleSend(payload, context.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(ChannelSendMessagePayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer player = context.player();

                    String channelId = payload.channelId();
                    String message = payload.message();

                    if (channelId == null || channelId.isBlank()) {
                        channelId = "global";
                    }

                    if (message == null) {
                        return;
                    }

                    message = message.trim();

                    if (message.isEmpty()) {
                        return;
                    }

                    boolean sent = ChannelChatService.handleChannelMessage(
                            player,
                            channelId,
                            message
                    );

                    if (sent || "global".equalsIgnoreCase(channelId)) {
                        ChatLogStorage.log(
                                player.server,
                                player,
                                channelId,
                                message
                        );
                    }
                })
        );
    }
}