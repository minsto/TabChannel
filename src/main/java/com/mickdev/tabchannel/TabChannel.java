package com.mickdev.tabchannel;


import com.mickdev.tabchannel.Commandes.ChannelCommands;
import com.mickdev.tabchannel.Common.ChatColors.Event.FabricGlobalChatStyleHandler;
import com.mickdev.tabchannel.Init.CommandRegistry;
import com.mickdev.tabchannel.NetWork.ModPayloads;
import com.mickdev.tabchannel.WindosConf.ChannelClientCommands;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


    public class TabChannel implements ModInitializer {

        public static final String MODID = "tabchannel";
        public static final String MOD_ID = MODID;
        public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

        @Override
        public void onInitialize() {
            ModPayloads.register();
            CommandRegistry.register();
            ChannelSyncEvents.register();
            Config.load();

            ChannelHudLayoutConfig.load(
                    FabricLoader.getInstance().getConfigDir()
            );

            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                ChannelHudLayoutConfig.ensureDefaultPosition();
            });

            FabricChatSearch.register();
            ChannelCommands.register();
            FabricGlobalChatStyleHandler.register();
            LOGGER.info("TabChannel Fabric initialized");
        }
    }


