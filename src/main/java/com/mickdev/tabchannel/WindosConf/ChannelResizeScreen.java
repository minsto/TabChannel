package com.mickdev.tabchannel.WindosConf;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ChannelResizeScreen extends Screen {

    private boolean dragging;

    public ChannelResizeScreen() {
        super(Component.literal("Resize TabChannel Chat"));
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

            int width = Math.max(
                    160,
                    (int) mouseX - ChannelHudLayoutConfig.chatX()
            );

            int height = Math.max(
                    60,
                    (int) mouseY - ChannelHudLayoutConfig.chatY()
            );

            ChannelHudLayoutConfig.setSize(width, height);
        }

        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = ChannelHudLayoutConfig.chatX();
        int y = ChannelHudLayoutConfig.chatY();
        int w = ChannelHudLayoutConfig.chatWidth();
        int h = ChannelHudLayoutConfig.chatHeight();

        g.fill(x, y, x + w, y + h, 0x5500E5FF);
        g.renderOutline(x, y, w, h, 0xFF00E5FF);

        g.drawCenteredString(
                font,
                "Drag with mouse to resize, release to save",
                width / 2,
                20,
                0xFFFFFF
        );

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
