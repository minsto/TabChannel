package com.mickdev.tabchannel.stream.commands;

import com.mickdev.tabchannel.TabChannel;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import com.mickdev.tabchannel.stream.gui.StreamChatConfigScreen;
import com.mickdev.tabchannel.stream.gui.StreamOverlayPositionScreen;
import com.mickdev.tabchannel.stream.gui.StreamOverlayResizeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class StreamChatClientCommands {

    private StreamChatClientCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("streamchat")
                        .then(Commands.literal("toggle")
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
                        .then(Commands.literal("position")
                                .then(Commands.literal("default")
                                        .executes(ctx -> {
                                            StreamOverlayLayoutConfig.resetPosition();
                                            return 1;
                                        }))
                                .executes(ctx -> {
                                    Minecraft.getInstance().setScreen(new StreamOverlayPositionScreen());
                                    return 1;
                                }))
                        .then(Commands.literal("resize")
                                .then(Commands.literal("default")
                                        .executes(ctx -> {
                                            StreamOverlayLayoutConfig.resetSize();
                                            return 1;
                                        }))
                                .executes(ctx -> {
                                    Minecraft.getInstance().setScreen(new StreamOverlayResizeScreen());
                                    return 1;
                                }))
                        .then(Commands.literal("config")
                                .executes(ctx -> {
                                    StreamChatConfigScreen.open();
                                    return 1;
                                }))
                        .then(Commands.literal("clear")
                                .executes(ctx -> {
                                    StreamChatManager.clearMessages();
                                    return 1;
                                }))
                        .then(Commands.literal("test")
                                .executes(ctx -> {
                                    StreamChatManager.addTestBatch();
                                    return 1;
                                }))
                        .then(Commands.literal("connect")
                                .executes(ctx -> {
                                    StreamChatManager.connectAll();
                                    return 1;
                                }))
                        .then(Commands.literal("disconnect")
                                .executes(ctx -> {
                                    StreamChatManager.disconnectAll();
                                    return 1;
                                }))
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    StreamChatConfig.reload();
                                    StreamOverlayLayoutConfig.bootstrap();
                                    return 1;
                                }))
        );
    }
}
