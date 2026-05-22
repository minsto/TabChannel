package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.Render.Hud.MpButtonRenderer;
import com.mickdev.tabchannel.Render.Hud.MpHudInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;

public final class ChannelChatScreenHook {
    private ChannelChatScreenHook() {}

    public static void render(ChatScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ChannelTabRenderer.render(screen, guiGraphics, mouseX, mouseY);
        ChannelMessageRenderer.render(screen, guiGraphics, mouseX, mouseY);

        Minecraft mc = Minecraft.getInstance();
        MpButtonRenderer.renderOnChat(guiGraphics, mc, mouseX, mouseY);
    }

    public static boolean mouseClicked(ChatScreen screen, double mouseX, double mouseY, int button) {
        if (MpHudInteraction.handlePopoutClick(mouseX, mouseY, button, Minecraft.getInstance())) {
            return true;
        }

        if (ChannelMessageRenderer.mouseClicked(screen, mouseX, mouseY, button)) {
            return true;
        }

        return ChannelTabRenderer.mouseClicked(screen, mouseX, mouseY, button);
    }

    public static boolean mouseScrolled(ChatScreen screen, double mouseX, double mouseY, double amount) {
        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();
        if (selectedChannelId == null || selectedChannelId.isBlank() || "global".equals(selectedChannelId)) {
            return false;
        }
        int visibleLines = ChannelMessageRenderer.getVisibleLineCount(screen);
        if (amount > 0) {
            ClientChannelChatState.scrollUp(selectedChannelId, 1, visibleLines);
            return true;
        } else if (amount < 0) {
            ClientChannelChatState.scrollDown(selectedChannelId, 1);
            return true;
        }
        return false;
    }
}
