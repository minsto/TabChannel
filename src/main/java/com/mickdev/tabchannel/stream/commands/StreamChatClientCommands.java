package com.mickdev.tabchannel.stream.commands;

import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import com.mickdev.tabchannel.stream.gui.StreamChatConfigScreen;
import com.mickdev.tabchannel.stream.gui.StreamOverlayLayoutMode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class StreamChatClientCommands {

    private StreamChatClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("streamchat")
                                .then(ClientCommandManager.literal("toggle")
                                        .executes(ctx -> {
                                            StreamOverlayLayoutConfig.toggleVisible();
                                            boolean on = StreamOverlayLayoutConfig.visible();
                                            Minecraft.getInstance().player.displayClientMessage(
                                                    Component.translatable(on
                                                            ? "tabchannel.stream.cmd.toggle_on"
                                                            : "tabchannel.stream.cmd.toggle_off"),
                                                    false
                                            );
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("position")
                                        .then(ClientCommandManager.literal("default")
                                                .executes(ctx -> {
                                                    StreamOverlayLayoutConfig.resetPosition();
                                                    return 1;
                                                }))
                                        .executes(ctx -> {
                                            StreamOverlayLayoutMode.enterPosition();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("resize")
                                        .then(ClientCommandManager.literal("default")
                                                .executes(ctx -> {
                                                    StreamOverlayLayoutConfig.resetSize();
                                                    return 1;
                                                }))
                                        .executes(ctx -> {
                                            StreamOverlayLayoutMode.enterResize();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("config")
                                        .executes(ctx -> {
                                            StreamChatConfigScreen.open();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("clear")
                                        .executes(ctx -> {
                                            StreamChatManager.clearMessages();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("test")
                                        .executes(ctx -> {
                                            StreamChatManager.addTestBatch();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("connect")
                                        .executes(ctx -> {
                                            StreamChatManager.connectAll();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("disconnect")
                                        .executes(ctx -> {
                                            StreamChatManager.disconnectAll();
                                            return 1;
                                        }))
                                .then(ClientCommandManager.literal("reload")
                                        .executes(ctx -> {
                                            StreamChatConfig.reload();
                                            StreamOverlayLayoutConfig.bootstrap();
                                            return 1;
                                        }))
                ));
    }
}
