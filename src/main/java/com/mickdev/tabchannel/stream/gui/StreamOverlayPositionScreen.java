package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class StreamOverlayPositionScreen extends StreamOverlayLayoutScreen {

    public StreamOverlayPositionScreen() {
        super(Component.translatable("tabchannel.stream.position.title"));
    }

    @Override
    protected void renderLayoutContent(GuiGraphics g, int mouseX, int mouseY) {
        int w = StreamOverlayLayoutConfig.width();
        int h = StreamOverlayLayoutConfig.height();
        drawPreviewBox(g, mouseX, mouseY, w, h);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.position.hint"), width / 2, 42, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clicksAllowed() || button != 0) {
            return true;
        }

        StreamOverlayLayoutConfig.setPosition((int) mouseX, (int) mouseY);
        saveAndClose();
        return true;
    }
}
