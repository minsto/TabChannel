package com.mickdev.tabchannel.Render.VanillaHud;



import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class ChannelIngameHudRenderer {

    private static final int LINE_HEIGHT = 9;

    // Position/taille proche du chat vanilla
    private static final int CHAT_X = 4;
    private static final int CHAT_BOTTOM = 40;
    private static final int CHAT_WIDTH = 320;
    private static final int MAX_LINES = 10;

    private static final long MESSAGE_LIFETIME_MS = 10_000L;

    private ChannelIngameHudRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.HOTBAR.equals(event.getName())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        // Quand le joueur ouvre le chat, on cache ce HUD
        if (mc.screen instanceof ChatScreen) {
            return;
        }

        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();
        if (selectedChannelId == null || selectedChannelId.isBlank() || "global".equals(selectedChannelId)) {
            return;
        }

        List<ClientChannelChatState.TimedMessage> messages =
                ClientChannelChatState.getTimedMessages(selectedChannelId);

        if (messages.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int bottom = screenHeight - CHAT_BOTTOM;
        int visibleCount = 0;

        // Compte seulement les messages encore visibles
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

        // fond comme vanilla, discret
        guiGraphics.fill(
                CHAT_X - 2,
                top - 1,
                CHAT_X + CHAT_WIDTH,
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
                    CHAT_X,
                    y,
                    0xFFFFFF,
                    false
            );

            y -= LINE_HEIGHT;
            shown++;
        }
    }
}