package com.mickdev.tabchannel;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import com.mickdev.tabchannel.Common.Mp.ClientMpAccess;
import com.mickdev.tabchannel.Common.Mp.ClientMpPersistence;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TabChannel.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public class TabChannelClient {
    public TabChannelClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ChannelHudLayoutConfig.bootstrap();
            StreamChatConfig.bootstrap();
            StreamOverlayLayoutConfig.bootstrap();
            StreamChatManager.bootstrap();
        });
        TabChannel.LOGGER.info("TabChannel Runs");
        TabChannel.LOGGER.info("TabChannel NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onPlayerJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        ChannelHudLayoutConfig.ensureDefaultPosition();
        ClientMpPersistence.loadForCurrentServer();
        StreamChatManager.bootstrap();
    }

    @SubscribeEvent
    static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientChannelTabState.resetForNewWorld();
        ClientChannelChatState.clearAll();
        ClientMpPersistence.resetServerContext();
        ClientMpAccess.reset();
        StreamChatManager.shutdown();
    }
}
