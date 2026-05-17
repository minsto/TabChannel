package com.mickdev.tabchannel.Init;

import com.mickdev.tabchannel.Commandes.ChannelCommands;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Youer (et autres hybrides NeoForge + Bukkit) : enregistrement en
 * {@link EventPriority#LOWEST} pour entrer après les patchs serveur, puis synchro explicite
 * côté Bukkit sur {@link ServerStartingEvent}.
 */
@EventBusSubscriber(modid = TabChannel.MODID)
public final class CommandRegistry {

    private CommandRegistry() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ChannelCommands.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        HybridCommandSupport.syncCommandsAfterRegistration(server);
    }
}
