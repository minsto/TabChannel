package com.mickdev.tabchannel.stream;

/**
 * Stream HUD is rendered from {@link com.mickdev.tabchannel.client.mixin.GuiStreamOverlayMixin}
 * at the end of {@link net.minecraft.client.gui.Gui#render} (same layer timing as NeoForge crosshair post).
 */
public final class StreamHudOverlay {

    private StreamHudOverlay() {
    }

    public static void register() {
        // Rendering handled by GuiStreamOverlayMixin on Fabric.
    }
}
