package com.mickdev.tabchannel.client;

import com.mickdev.tabchannel.Render.Gui.ChannelClientGui;
import com.mickdev.tabchannel.WindosConf.ChannelClientCommands;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutScheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * Client-only commands (work on any server, including Youer without TabChannel server mod).
 */
public final class TabChannelClientCommands {

    private TabChannelClientCommands() {
    }

    public static void register() {
        ChannelHudLayoutScheduler.register();
        ChannelClientCommands.register();

        ClientCommandRegistrationCallback.EVENT.register(TabChannelClientCommands::registerCommands);
    }

    private static void registerCommands(
            com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess
    ) {
        var gui = ClientCommandManager.literal("gui")
                .executes(ctx -> {
                    ChannelClientGui.openMain();
                    return 1;
                });

        var help = ClientCommandManager.literal("help")
                .executes(ctx -> {
                    if (ctx.getSource().getPlayer() != null) {
                        ctx.getSource().getPlayer().displayClientMessage(
                                Component.translatable("tabchannel.client.commands_help"),
                                false
                        );
                    }
                    return 1;
                });

        var root = ClientCommandManager.literal("tabchannel")
                .then(gui)
                .then(help);

        dispatcher.register(root);
        dispatcher.register(ClientCommandManager.literal("tc").then(gui).then(help));
    }
}
