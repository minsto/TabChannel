package com.mickdev.tabchannel.Render.Hud;

import com.mickdev.tabchannel.Common.Mp.ClientMpNotifications;
import com.mickdev.tabchannel.TabChannel;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public final class MpButtonRenderer {

    public static final ResourceLocation ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "textures/gui/mp_button.png");

    public static final int ICON_SIZE = 16;
    public static final int PADDING = 4;
    public static final int BTN_W = ICON_SIZE + PADDING * 2;
    public static final int BTN_H = ICON_SIZE + PADDING * 2;
    private static final int MARGIN = 10;
    private static final int BADGE_SIZE = 6;

    private MpButtonRenderer() {
    }

    public static boolean shouldShowOnHud(Minecraft mc) {
        if (mc.player == null || mc.options.hideGui) {
            return false;
        }

        if (mc.screen instanceof ChatScreen) {
            return false;
        }

        return ClientMpNotifications.hasHudAlert();
    }

    public static boolean shouldShowOnChat(Minecraft mc) {
        return mc.player != null && mc.screen instanceof ChatScreen;
    }

    public static int buttonX(Minecraft mc) {
        return mc.getWindow().getGuiScaledWidth() - BTN_W - MARGIN;
    }

    public static int buttonY() {
        return 8;
    }

    public static boolean isMouseOver(double mouseX, double mouseY, Minecraft mc) {
        int x = buttonX(mc);
        int y = buttonY();
        return mouseX >= x && mouseX <= x + BTN_W && mouseY >= y && mouseY <= y + BTN_H;
    }

    public static void renderOnChat(GuiGraphics g, Minecraft mc, int mouseX, int mouseY) {
        if (!shouldShowOnChat(mc)) {
            return;
        }

        draw(g, mc, mouseX, mouseY);
    }

    public static void renderOnHud(GuiGraphics g, Minecraft mc, int mouseX, int mouseY) {
        if (!shouldShowOnHud(mc)) {
            return;
        }

        draw(g, mc, mouseX, mouseY);
    }

    private static void draw(GuiGraphics g, Minecraft mc, int mouseX, int mouseY) {
        int x = buttonX(mc);
        int y = buttonY();
        int iconX = x + PADDING;
        int iconY = y + PADDING;

        if (isMouseOver(mouseX, mouseY, mc)) {
            g.fill(x, y, x + BTN_W, y + BTN_H, 0x880B2A35);
        }

        ensureNearestFilter(mc.getTextureManager());

        g.blit(
                ICON_TEXTURE,
                iconX,
                iconY,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE
        );

        if (ClientMpNotifications.hasHudAlert()) {
            int badgeX = iconX + ICON_SIZE - BADGE_SIZE + 1;
            int badgeY = iconY - 1;
            g.fill(badgeX, badgeY, badgeX + BADGE_SIZE, badgeY + BADGE_SIZE, 0xFFEF4444);
        }
    }

    private static void ensureNearestFilter(TextureManager textures) {
        var texture = textures.getTexture(ICON_TEXTURE);

        if (texture != null) {
            RenderSystem.setShaderTexture(0, ICON_TEXTURE);
            texture.setFilter(false, false);
        }
    }
}
