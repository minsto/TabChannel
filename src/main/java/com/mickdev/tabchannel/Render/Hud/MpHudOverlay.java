package com.mickdev.tabchannel.Render.Hud;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class MpHudOverlay {

    private static boolean leftWasDown;

    private MpHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (!MpButtonRenderer.shouldShowOnHud(mc)) {
            return;
        }

        int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
        MpButtonRenderer.renderOnHud(event.getGuiGraphics(), mc, mouseX, mouseY);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (!MpHudInteraction.allowsHudTickClick(mc) || mc.player == null) {
            leftWasDown = false;
            return;
        }

        long window = mc.getWindow().getWindow();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        double mx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

        if (leftDown && !leftWasDown && MpHudInteraction.isOverPopoutButton(mx, my, mc)) {
            MpHudInteraction.openMpGui(mc);
        }

        leftWasDown = leftDown;
    }
}
