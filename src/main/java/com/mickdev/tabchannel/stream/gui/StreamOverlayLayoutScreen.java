package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class StreamOverlayLayoutScreen extends Screen {

    private static final int CLICK_WARMUP_FRAMES = 8;

    protected final boolean restoreVisible;
    private int clickWarmup = CLICK_WARMUP_FRAMES;

    protected StreamOverlayLayoutScreen(Component title) {
        super(title);
        restoreVisible = StreamOverlayLayoutConfig.visible();
        StreamOverlayLayoutConfig.setVisible(true);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (clickWarmup > 0) {
            clickWarmup--;
        }

        renderDimBackground(g);
        renderLayoutContent(g, mouseX, mouseY);
        drawHeader(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    protected abstract void renderLayoutContent(GuiGraphics g, int mouseX, int mouseY);

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            cancel();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected boolean clicksAllowed() {
        return clickWarmup <= 0;
    }

    protected void saveAndClose() {
        StreamOverlayLayoutConfig.setVisible(restoreVisible);
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("tabchannel.stream.layout.saved"), false);
        }
        onClose();
    }

    protected void cancel() {
        StreamOverlayLayoutConfig.setVisible(restoreVisible);
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("tabchannel.stream.layout.cancelled"), false);
        }
        onClose();
    }

    private void renderDimBackground(GuiGraphics g) {
        g.fill(0, 0, width, height, 0x90000000);
    }

    private void drawHeader(GuiGraphics g) {
        g.drawCenteredString(font, title, width / 2, 12, 0xFF45F3FF);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.layout.esc_hint"), width / 2, 26, 0xFFAAAAAA);
    }

    protected static void drawPreviewBox(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xD900FFFF);
        for (int i = 0; i < 3; i++) {
            g.renderOutline(x - i, y - i, w + i * 2, h + i * 2, 0xFF00FFFF);
        }
        g.fill(x + w - 10, y + h - 10, x + w, y + h, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
