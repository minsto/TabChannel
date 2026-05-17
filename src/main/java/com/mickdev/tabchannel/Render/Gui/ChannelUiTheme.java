package com.mickdev.tabchannel.Render.Gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ChannelUiTheme {

    public static final int BG_DARK = 0xD80B0D14;
    public static final int BG_PANEL = 0xCC111827;
    public static final int BG_PANEL_LIGHT = 0xCC1F2937;
    public static final int CYAN = 0xFF45F3FF;
    public static final int CYAN_SOFT = 0x8845F3FF;
    public static final int BLUE = 0xFF60A5FA;
    public static final int GOLD = 0xFFFFD166;
    public static final int RED = 0xFFFF5555;
    public static final int GREEN = 0xFF55FF88;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFF9CA3AF;

    private ChannelUiTheme() {
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BG_PANEL);
        border(g, x, y, w, h, CYAN_SOFT);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0x5545F3FF);
    }

    public static void card(GuiGraphics g, int x, int y, int w, int h, boolean active) {
        g.fill(x, y, x + w, y + h, active ? 0xCC153449 : BG_PANEL_LIGHT);
        border(g, x, y, w, h, active ? CYAN : 0x553B82F6);
    }

    public static void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void centered(GuiGraphics g, Component text, int x, int y, int w, int color) {
        Minecraft mc = Minecraft.getInstance();
        int tx = x + (w - mc.font.width(text)) / 2;
        g.drawString(mc.font, text, tx, y, color, false);
    }
}
