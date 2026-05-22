package com.mickdev.tabchannel.stream;

import com.mickdev.tabchannel.stream.gui.StreamChatOverlayRenderer;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class StreamHudOverlay {

    private StreamHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        StreamChatOverlayRenderer.render(event.getGuiGraphics());
    }
}
