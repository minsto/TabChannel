package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.StreamChatMessage;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class StreamChatOverlayRenderer {

    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm");

    private StreamChatOverlayRenderer() {
    }

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        if (!StreamOverlayLayoutConfig.visible()) {
            return;
        }

        int x = StreamOverlayLayoutConfig.x();
        int y = StreamOverlayLayoutConfig.y();
        int w = StreamOverlayLayoutConfig.width();
        int h = StreamOverlayLayoutConfig.height();
        float opacity = StreamOverlayLayoutConfig.opacity();

        int bgAlpha = (int) (opacity * 200) << 24;
        int borderAlpha = (int) (opacity * 255) << 24;

        g.fill(x, y, x + w, y + h, bgAlpha | 0x101010);
        g.fill(x, y, x + w, y + 1, borderAlpha | 0x00E5FF);
        g.fill(x, y + h - 1, x + w, y + h, borderAlpha | 0x003344);

        g.drawString(mc.font, Component.literal("[STREAM]"), x + 6, y + 4, 0xFF45F3FF, false);

        Component status = StreamChatManager.statusComponent();
        if (status != null && !status.getString().isBlank()) {
            String plain = status.getString();
            String shortStatus = mc.font.plainSubstrByWidth(plain, w - 70) + (mc.font.width(plain) > w - 70 ? "..." : "");
            g.drawString(mc.font, Component.literal(shortStatus), x + 70, y + 4, 0xFFAAAAAA, false);
        }

        List<StreamChatMessage> messages = StreamChatManager.getMessages();
        int lineY = y + 18;
        int maxY = y + h - 6;
        int lineH = 10;
        int visible = Math.max(1, (h - 22) / lineH);
        int start = Math.max(0, messages.size() - visible);

        for (int i = start; i < messages.size(); i++) {
            if (lineY > maxY) {
                break;
            }

            StreamChatMessage msg = messages.get(i);
            String time = TIME.format(new Date(msg.timestampMs()));
            String platform = "[" + msg.platform().label() + "]";
            String badge = msg.badgeText() == null ? "" : msg.badgeText() + " ";
            String line = time + " " + platform + " " + badge + msg.formattedName() + ": " + msg.message();

            int color = msg.colorArgb() != 0 ? msg.colorArgb() : 0xFFE5E7EB;
            String clipped = mc.font.plainSubstrByWidth(line, w - 12);
            g.drawString(mc.font, Component.literal(clipped), x + 6, lineY, color, false);
            lineY += lineH;
        }
    }
}
