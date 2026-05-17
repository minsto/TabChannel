package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.TabChannel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class ChannelGuiKeyHandler {

    private static final KeyMapping OPEN_CHANNEL_GUI = new KeyMapping(
            "key.tabchannel.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.tabchannel"
    );

    private ChannelGuiKeyHandler() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHANNEL_GUI);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        while (OPEN_CHANNEL_GUI.consumeClick()) {
            if (mc.screen == null) {
                ChannelClientGui.openMain();
            }
        }
    }
}
