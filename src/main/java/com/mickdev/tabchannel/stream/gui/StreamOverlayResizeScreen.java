package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class StreamOverlayResizeScreen extends Screen {

    private boolean dragging;

    public StreamOverlayResizeScreen() {
        super(Component.translatable("tabchannel.stream.resize.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = true;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        onClose();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            int newW = Math.max(StreamOverlayLayoutConfig.MIN_WIDTH, (int) mouseX - StreamOverlayLayoutConfig.x());
            int newH = Math.max(StreamOverlayLayoutConfig.MIN_HEIGHT, (int) mouseY - StreamOverlayLayoutConfig.y());
            StreamOverlayLayoutConfig.setSize(newW, newH);
        }

        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = StreamOverlayLayoutConfig.x();
        int y = StreamOverlayLayoutConfig.y();
        int w = StreamOverlayLayoutConfig.width();
        int h = StreamOverlayLayoutConfig.height();

        g.fill(x, y, x + w, y + h, 0x8800E5FF);
        g.renderOutline(x, y, w, h, 0xFF00E5FF);

        g.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.resize.hint"), width / 2, 36, 0xFFAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
