package com.mickdev.tabchannel.NetWork;

import com.mickdev.tabchannel.NetWork.CodecChanel.*;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelClearMessagesPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpBrowseModePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpMessagePayload;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import com.mickdev.tabchannel.WindosConf.ChannelPositionScreen;
import com.mickdev.tabchannel.WindosConf.ChannelResizeScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class ClientModPayloads {
    private ClientModPayloads() {}

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ChannelClearTabsPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleClearTabs(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ChannelAddTabPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleAddTab(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ChannelClearMessagesPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleClearMessages(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ChannelMessagePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleChannelMessage(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ChannelMentionNotifyPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleMentionNotify(payload)));
        ClientPlayNetworking.registerGlobalReceiver(MpMessagePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleMpMessage(payload)));
        ClientPlayNetworking.registerGlobalReceiver(MpBrowseModePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ChannelClientPayloadHandler.handleMpBrowseMode(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ChannelLayoutPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft mc = Minecraft.getInstance();

                switch (payload.action()) {

                    case "resize" -> mc.setScreen(new ChannelResizeScreen());

                    case "position" -> mc.setScreen(new ChannelPositionScreen());

                    case "resize_default" -> {

                        ChannelHudLayoutConfig.resetSize();

                        ChannelHudLayoutConfig.resetPosition(
                                mc.getWindow().getGuiScaledHeight()
                        );
                    }

                    case "position_default" ->

                            ChannelHudLayoutConfig.resetPosition(
                                    mc.getWindow().getGuiScaledHeight()
                            );
                }
            });
        });
    }
}
