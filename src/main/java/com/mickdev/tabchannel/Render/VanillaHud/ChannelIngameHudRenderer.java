package com.mickdev.tabchannel.Render.VanillaHud;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ChannelIngameHudRenderer {

    private static final int LINE_HEIGHT = 9;
    private static final int MAX_LINES = 10;
    private static final long MESSAGE_LIFETIME_MS = 10_000L;

    private ChannelIngameHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> render(guiGraphics));
    }

    private static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        if (mc.screen instanceof ChatScreen) {
            return;
        }

        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();

        if (selectedChannelId == null || selectedChannelId.isBlank() || "global".equalsIgnoreCase(selectedChannelId)) {
            return;
        }

        List<ClientChannelChatState.TimedMessage> messages =
                ClientChannelChatState.getTimedMessages(selectedChannelId);

        if (messages.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        int chatX = ChannelHudLayoutConfig.chatX();
        int chatWidth = ChannelHudLayoutConfig.chatWidth();
        int bottom = ChannelHudLayoutConfig.chatY() + ChannelHudLayoutConfig.chatHeight();

        int visibleCount = 0;

        for (int i = messages.size() - 1; i >= 0 && visibleCount < MAX_LINES; i--) {
            ClientChannelChatState.TimedMessage entry = messages.get(i);

            if (now - entry.timeMs() <= MESSAGE_LIFETIME_MS) {
                visibleCount++;
            }
        }

        if (visibleCount <= 0) {
            return;
        }

        int boxHeight = (visibleCount * LINE_HEIGHT) + 4;
        int top = bottom - boxHeight;

        guiGraphics.fill(
                chatX - 2,
                top - 1,
                chatX + chatWidth,
                bottom + 1,
                0x7F000000
        );

        int y = bottom - LINE_HEIGHT;
        int shown = 0;

        for (int i = messages.size() - 1; i >= 0 && shown < MAX_LINES; i--) {
            ClientChannelChatState.TimedMessage entry = messages.get(i);

            if (now - entry.timeMs() > MESSAGE_LIFETIME_MS) {
                continue;
            }

            Component line = entry.message();

            guiGraphics.drawString(
                    mc.font,
                    line,
                    chatX,
                    y,
                    0xFFFFFF,
                    false
            );

            y -= LINE_HEIGHT;
            shown++;
        }
    }
}