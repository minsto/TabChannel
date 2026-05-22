package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class StreamOverlayPositionScreen extends Screen {

    public StreamOverlayPositionScreen() {
        super(Component.translatable("tabchannel.stream.position.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        StreamOverlayLayoutConfig.setPosition((int) mouseX, (int) mouseY);
        onClose();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int w = StreamOverlayLayoutConfig.width();
        int h = StreamOverlayLayoutConfig.height();

        g.fill(mouseX, mouseY, mouseX + w, mouseY + h, 0x8800E5FF);
        g.renderOutline(mouseX, mouseY, w, h, 0xFF00E5FF);

        g.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.position.hint"), width / 2, 36, 0xFFAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
