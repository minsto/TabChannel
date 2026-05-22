package com.mickdev.tabchannel.WindosConf;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public final class ChannelClientCommands {

    private ChannelClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("channel")
                        .then(ClientCommandManager.literal("resize")
                                .then(ClientCommandManager.literal("default")
                                        .executes(ctx -> {
                                            ChannelHudLayoutConfig.resetSize();
                                            Minecraft mc = Minecraft.getInstance();
                                            ChannelHudLayoutConfig.resetPosition(mc.getWindow().getGuiScaledHeight());
                                            return 1;
                                        }))
                                .executes(ctx -> {
                                    ChannelHudLayoutScheduler.openResize();
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("position")
                                .then(ClientCommandManager.literal("default")
                                        .executes(ctx -> {
                                            Minecraft mc = Minecraft.getInstance();
                                            ChannelHudLayoutConfig.resetPosition(mc.getWindow().getGuiScaledHeight());
                                            return 1;
                                        }))
                                .executes(ctx -> {
                                    ChannelHudLayoutScheduler.openPosition();
                                    return 1;
                                }))
                ));
    }
}
