package com.mickdev.tabchannel.stream.keybinds;

import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import com.mickdev.tabchannel.stream.gui.StreamChatConfigScreen;
import com.mickdev.tabchannel.stream.gui.StreamChatInputScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

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

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_OVERLAY);
        KeyBindingHelper.registerKeyBinding(OPEN_INPUT);
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG);
        KeyBindingHelper.registerKeyBinding(CLEAR_CHAT);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
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
        });
    }
}
