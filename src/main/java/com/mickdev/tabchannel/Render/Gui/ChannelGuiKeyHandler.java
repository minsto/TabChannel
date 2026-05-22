package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.Render.Hud.MpHudInteraction;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ChannelGuiKeyHandler {
    private static final KeyMapping OPEN_CHANNEL_GUI = new KeyMapping(
            "key.tabchannel.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.tabchannel"
    );

    private static final KeyMapping OPEN_MP_GUI = new KeyMapping(
            "key.tabchannel.open_mp",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.tabchannel"
    );

    private ChannelGuiKeyHandler() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_CHANNEL_GUI);
        KeyBindingHelper.registerKeyBinding(OPEN_MP_GUI);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            while (OPEN_CHANNEL_GUI.consumeClick()) {
                if (mc.screen == null) ChannelClientGui.openMain();
            }
            while (OPEN_MP_GUI.consumeClick()) {
                MpHudInteraction.openMpGui(mc);
            }
        });
    }
}
