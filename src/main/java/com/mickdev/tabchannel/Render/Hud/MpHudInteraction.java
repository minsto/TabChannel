package com.mickdev.tabchannel.Render.Hud;

import com.mickdev.tabchannel.Render.Gui.screens.MpChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public final class MpHudInteraction {

    private MpHudInteraction() {
    }

    public static boolean isOverPopoutButton(double mouseX, double mouseY, Minecraft mc) {
        if (mc.screen instanceof ChatScreen) {
            return MpButtonRenderer.shouldShowOnChat(mc) && MpButtonRenderer.isMouseOver(mouseX, mouseY, mc);
        }

        return MpButtonRenderer.shouldShowOnHud(mc) && MpButtonRenderer.isMouseOver(mouseX, mouseY, mc);
    }

    public static boolean allowsHudTickClick(Minecraft mc) {
        return MpButtonRenderer.shouldShowOnHud(mc);
    }

    public static boolean handlePopoutClick(double mouseX, double mouseY, int button, Minecraft mc) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || mc.player == null) {
            return false;
        }

        if (!isOverPopoutButton(mouseX, mouseY, mc)) {
            return false;
        }

        openMpGui(mc);
        return true;
    }

    public static void openMpGui(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        MpChatScreen.open();
    }

    public static boolean isBlockingScreen(Screen screen) {
        return screen != null && !(screen instanceof ChatScreen) && !(screen instanceof MpChatScreen);
    }
}
