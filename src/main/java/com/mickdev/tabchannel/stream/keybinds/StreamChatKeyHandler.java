package com.mickdev.tabchannel.stream.keybinds;

import com.mickdev.tabchannel.TabChannel;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import com.mickdev.tabchannel.stream.gui.StreamChatConfigScreen;
import com.mickdev.tabchannel.stream.gui.StreamChatInputScreen;
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
public final class StreamChatKeyHandler {

    private static final KeyMapping TOGGLE_OVERLAY = new KeyMapping(
            "key.tabchannel.stream.toggle_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.tabchannel.stream"
    );

    private static final KeyMapping OPEN_INPUT = new KeyMapping(
            "key.tabchannel.stream.open_input",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.tabchannel.stream"
    );

    private static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.tabchannel.stream.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.tabchannel.stream"
    );

    private static final KeyMapping CLEAR_CHAT = new KeyMapping(
            "key.tabchannel.stream.clear",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.tabchannel.stream"
    );

    private StreamChatKeyHandler() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY);
        event.register(OPEN_INPUT);
        event.register(OPEN_CONFIG);
        event.register(CLEAR_CHAT);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        while (TOGGLE_OVERLAY.consumeClick()) {
            StreamOverlayLayoutConfig.toggleVisible();
        }

        while (OPEN_INPUT.consumeClick()) {
            StreamChatInputScreen.open();
        }

        while (OPEN_CONFIG.consumeClick()) {
            StreamChatConfigScreen.open();
        }

        while (CLEAR_CHAT.consumeClick()) {
            StreamChatManager.clearMessages();
        }
    }
}
