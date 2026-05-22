package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class StreamChatInputScreen extends Screen {

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 108;

    private EditBox input;
    private int panelX;
    private int panelY;
    private String platformTag = "";

    public StreamChatInputScreen() {
        super(Component.translatable("tabchannel.stream.input.title"));
    }

    public static void open() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new StreamChatInputScreen());
    }

    @Override
    protected void init() {
        platformTag = StreamChatConfig.selectedSendPlatform().label().toUpperCase();

        panelX = width / 2 - PANEL_W / 2;
        panelY = height / 2 - PANEL_H / 2;

        input = new EditBox(font, panelX + 10, panelY + 58, PANEL_W - 20, 20, Component.empty());
        input.setMaxLength(256);
        input.setBordered(true);
        input.setTextColor(ChannelUiTheme.WHITE);
        input.setTextColorUneditable(ChannelUiTheme.GRAY);
        addRenderableWidget(input);

        int btnY = panelY + PANEL_H - 28;
        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.input.send"), b -> send())
                .bounds(panelX + 10, btnY, 100, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.gui.button.close"), b -> onClose())
                .bounds(panelX + PANEL_W - 90, btnY, 80, 20)
                .build());

        setInitialFocus(input);
    }

    private void send() {
        if (input == null) {
            return;
        }

        String text = input.getValue().trim();

        if (!text.isEmpty()) {
            StreamChatManager.sendToSelectedPlatform(text);
            input.setValue("");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            send();
            return true;
        }

        if (input != null && input.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        ChannelUiTheme.panel(g, panelX, panelY, PANEL_W, PANEL_H);
        super.render(g, mouseX, mouseY, partialTick);
        drawLabels(g);
    }

    private void drawLabels(GuiGraphics g) {
        int centerX = width / 2;
        Component header = Component.literal("[STREAM] [" + platformTag + "]");
        Component subtitle = Component.translatable("tabchannel.stream.input.subtitle");
        Component hint = Component.translatable("tabchannel.stream.input.hint", platformTag);

        g.drawCenteredString(font, header, centerX, panelY + 12, ChannelUiTheme.CYAN);
        g.drawCenteredString(font, subtitle, centerX, panelY + 28, ChannelUiTheme.WHITE);
        g.drawCenteredString(font, hint, centerX, panelY + 42, ChannelUiTheme.GOLD);
        g.drawCenteredString(font, Component.translatable("tabchannel.stream.input.enter_hint"), centerX, panelY + 54, ChannelUiTheme.GRAY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
