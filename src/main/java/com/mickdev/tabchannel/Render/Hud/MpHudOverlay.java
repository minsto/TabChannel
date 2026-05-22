package com.mickdev.tabchannel.Render.Hud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public final class MpHudOverlay {

    private static boolean leftWasDown;

    private MpHudOverlay() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();

            if (!MpButtonRenderer.shouldShowOnHud(mc)) {
                return;
            }

            int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
            MpButtonRenderer.renderOnHud(graphics, mc, mouseX, mouseY);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!MpHudInteraction.allowsHudTickClick(client) || client.player == null) {
                leftWasDown = false;
                return;
            }

            long window = client.getWindow().getWindow();
            boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

            double mx = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
            double my = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();

            if (leftDown && !leftWasDown && MpHudInteraction.isOverPopoutButton(mx, my, client)) {
                MpHudInteraction.openMpGui(client);
            }

            leftWasDown = leftDown;
        });
    }
}
