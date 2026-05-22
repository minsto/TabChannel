package com.mickdev.tabchannel.Init;

import com.mickdev.tabchannel.Commandes.ChannelCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class CommandRegistry {
    private CommandRegistry() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ChannelCommands.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(CommandRegistry::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        HybridBukkitPermissions.registerDefaultsForAllPlayers();
        HybridCommandSupport.syncCommandsAfterRegistration(server);
    }
}
