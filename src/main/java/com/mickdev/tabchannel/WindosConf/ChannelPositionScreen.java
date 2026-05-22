package com.mickdev.tabchannel.WindosConf;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ChannelPositionScreen extends Screen {

    public ChannelPositionScreen() {
        super(Component.literal("Move TabChannel Chat"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        ChannelHudLayoutConfig.setPosition(
                (int) mouseX,
                (int) mouseY
        );

        onClose();

        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int w = ChannelHudLayoutConfig.chatWidth();
        int h = ChannelHudLayoutConfig.chatHeight();

        g.fill(mouseX, mouseY, mouseX + w, mouseY + h, 0x5500E5FF);
        g.renderOutline(mouseX, mouseY, w, h, 0xFF00E5FF);

        g.drawCenteredString(
                font,
                "Move mouse, click to save position",
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
