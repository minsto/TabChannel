package com.mickdev.tabchannel.client;

import com.mickdev.tabchannel.NetWork.ClientModPayloads;
import com.mickdev.tabchannel.Common.Mp.ClientMpAccess;
import com.mickdev.tabchannel.Common.Mp.ClientMpPersistence;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.Render.Gui.ChannelGuiKeyHandler;
import com.mickdev.tabchannel.Render.Hud.ChannelNotificationHud;
import com.mickdev.tabchannel.Render.Hud.MpHudOverlay;
import com.mickdev.tabchannel.Render.VanillaHud.ChannelIngameHudRenderer;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.StreamHudOverlay;
import com.mickdev.tabchannel.client.TabChannelClientCommands;
import com.mickdev.tabchannel.stream.commands.StreamChatClientCommands;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import com.mickdev.tabchannel.stream.gui.StreamOverlayLayoutScheduler;
import com.mickdev.tabchannel.stream.keybinds.StreamChatKeyHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class TabChannelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientModPayloads.registerClientReceivers();
        ChannelClientSyncGuard.register();
        ChannelGuiKeyHandler.register();
        ChannelIngameHudRenderer.register();
        ChannelNotificationHud.register();
        MpHudOverlay.register();
        StreamHudOverlay.register();
        StreamOverlayLayoutScheduler.register();
        StreamChatKeyHandler.register();
        TabChannelClientCommands.register();
        StreamChatClientCommands.register();
        StreamChatConfig.bootstrap();
        StreamOverlayLayoutConfig.bootstrap();
        StreamChatManager.bootstrap();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientMpPersistence.loadForCurrentServer();
            StreamOverlayLayoutConfig.ensureDefaultPosition();
            StreamChatManager.bootstrap();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientChannelTabState.resetForNewWorld();
            ClientChannelChatState.clearAll();
            ClientMpPersistence.resetServerContext();
            ClientMpAccess.reset();
            StreamChatManager.shutdown();
        });
    }
}
