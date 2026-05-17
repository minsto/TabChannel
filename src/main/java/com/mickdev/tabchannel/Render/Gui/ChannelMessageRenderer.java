package com.mickdev.tabchannel.Render.Gui;





import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ChannelMessageRenderer {

    private static final int LINE_HEIGHT = 9;

    // Position proche du vrai chat vanilla
    private static final int CHAT_X = 4;
    private static final int CHAT_BOTTOM_MARGIN = 38;
    private static final int CHAT_WIDTH = 320;
    private static final int CHAT_HEIGHT = 180;

    private static final int SCROLLBAR_WIDTH = 6;

    private ChannelMessageRenderer() {
    }

    public static int getVisibleLineCount(Screen screen) {
        return Math.max(1, CHAT_HEIGHT / LINE_HEIGHT);
    }

    public static void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen instanceof ChatScreen)) {
            return;
        }

        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();
        if (selectedChannelId == null || selectedChannelId.isBlank() || "global".equals(selectedChannelId)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        List<Component> messages = ClientChannelChatState.getMessages(selectedChannelId);

        int left = CHAT_X;
        int bottom = screen.height - CHAT_BOTTOM_MARGIN;
        int top = bottom - CHAT_HEIGHT;
        int right = left + CHAT_WIDTH;

        int visibleLines = Math.max(1, CHAT_HEIGHT / LINE_HEIGHT);
        int scrollOffset = ClientChannelChatState.getScrollOffset(selectedChannelId);

        int endExclusive = Math.max(0, messages.size() - scrollOffset);
        int startInclusive = Math.max(0, endExclusive - visibleLines);

        // fond style vanilla
        guiGraphics.fill(
                left - 2,
                top - 2,
                right + 2,
                bottom + 2,
                0x7F000000
        );

        int y = bottom - LINE_HEIGHT - 2;
        for (int i = endExclusive - 1; i >= startInclusive; i--) {
            guiGraphics.drawString(
                    mc.font,
                    messages.get(i),
                    left,
                    y,
                    0xFFFFFF,
                    false
            );
            y -= LINE_HEIGHT;
        }

        renderScrollbar(guiGraphics, selectedChannelId, top, bottom, visibleLines, messages.size(), right);
    }

    private static void renderScrollbar(
            GuiGraphics guiGraphics,
            String channelId,
            int top,
            int bottom,
            int visibleLines,
            int totalMessages,
            int right
    ) {
        if (totalMessages <= visibleLines) {
            return;
        }

        int trackX1 = right - SCROLLBAR_WIDTH;
        int trackX2 = right;
        int trackY1 = top;
        int trackY2 = bottom;

        int trackHeight = trackY2 - trackY1;
        if (trackHeight <= 0) {
            return;
        }

        guiGraphics.fill(trackX1, trackY1, trackX2, trackY2, 0x50000000);

        int maxScroll = ClientChannelChatState.getMaxScrollOffset(channelId, visibleLines);
        int scrollOffset = ClientChannelChatState.getScrollOffset(channelId);

        int thumbHeight = Math.max(20, (visibleLines * trackHeight) / Math.max(totalMessages, 1));
        int movableHeight = trackHeight - thumbHeight;

        int thumbY;
        if (maxScroll <= 0) {
            thumbY = trackY1;
        } else {
            float progress = (float) scrollOffset / (float) maxScroll;
            thumbY = trackY1 + Math.round(progress * movableHeight);
        }

        guiGraphics.fill(trackX1, thumbY, trackX2, thumbY + thumbHeight, 0xCCAAAAAA);
        guiGraphics.fill(trackX1, thumbY, trackX2 - 1, thumbY + 1, 0xFFFFFFFF);
        guiGraphics.fill(trackX1, thumbY, trackX1 + 1, thumbY + thumbHeight, 0xFFFFFFFF);
    }
}