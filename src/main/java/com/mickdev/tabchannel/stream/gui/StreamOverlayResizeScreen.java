package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class StreamOverlayResizeScreen extends StreamOverlayLayoutScreen {

    private boolean dragging;

    public StreamOverlayResizeScreen() {
        super(Component.translatable("tabchannel.stream.resize.title"));
    }

    @Override
    protected void renderLayoutContent(GuiGraphics g, int mouseX, int mouseY) {
        int x = StreamOverlayLayoutConfig.x();
        int y = StreamOverlayLayoutConfig.y();
        int w = StreamOverlayLayoutConfig.width();
        int h = StreamOverlayLayoutConfig.height();
        drawPreviewBox(g, x, y, w, h);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.resize.hint"), width / 2, 42, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clicksAllowed() || button != 0) {
            return true;
        }
        dragging = true;
        applySize(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            applySize(mouseX, mouseY);
            saveAndClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            applySize(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private static void applySize(double mouseX, double mouseY) {
        int newW = Math.max(StreamOverlayLayoutConfig.MIN_WIDTH, (int) mouseX - StreamOverlayLayoutConfig.x());
        int newH = Math.max(StreamOverlayLayoutConfig.MIN_HEIGHT, (int) mouseY - StreamOverlayLayoutConfig.y());
        StreamOverlayLayoutConfig.setSize(newW, newH);
    }
}
