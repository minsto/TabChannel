package com.mickdev.tabchannel.Render.Hud;

import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import com.mickdev.tabchannel.Render.Gui.ClientChannelNotifications;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class ChannelNotificationHud {

    private ChannelNotificationHud() {
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.HOTBAR.equals(event.getName())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !ClientChannelNotifications.hasFreshPing()) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int x = w - 238;
        int y = 28;
        int boxW = 226;
        int boxH = 42;

        ChannelUiTheme.panel(g, x, y, boxW, boxH);
        g.drawString(mc.font, Component.translatable("tabchannel.hud.ping", ClientChannelNotifications.getLastPingChannel()), x + 8, y + 7, ChannelUiTheme.CYAN, false);

        String msg = ClientChannelNotifications.getLastPing().getString();
        if (mc.font.width(msg) > boxW - 16) {
            msg = mc.font.plainSubstrByWidth(msg, boxW - 28) + "...";
        }
        g.drawString(mc.font, msg, x + 8, y + 23, ChannelUiTheme.WHITE, false);
    }
}
