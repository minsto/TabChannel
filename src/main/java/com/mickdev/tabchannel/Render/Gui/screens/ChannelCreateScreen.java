package com.mickdev.tabchannel.Render.Gui.screens;
import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ChannelCreateScreen extends Screen {

    private final Screen parent;

    private EditBox nameBox;
    private String type = "public";

    private int panelX, panelY, panelW, panelH;

    public ChannelCreateScreen(Screen parent) {
        super(Component.translatable("tabchannel.gui.create.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(430, this.width - 40);
        panelH = 220;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        nameBox = new EditBox(
                font,
                panelX + 24,
                panelY + 58,
                panelW - 48,
                22,
                Component.translatable("tabchannel.gui.create.name")
        );
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.translatable("tabchannel.gui.create.name_hint"));
        addRenderableWidget(nameBox);

        int y = panelY + 92;

        addRenderableWidget(Button.builder(Component.literal("Public"), b -> type = "public")
                .bounds(panelX + 24, y, 115, 22)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Private"), b -> type = "private")
                .bounds(panelX + 157, y, 115, 22)
                .build());



        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.create"),
                        b -> createChannel()
                )
                .bounds(panelX + 24, panelY + panelH - 38, 150, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.back"),
                        b -> minecraft.setScreen(parent)
                )
                .bounds(panelX + panelW - 174, panelY + panelH - 38, 150, 22)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Vide = pas de blur, monde visible derrière
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderPanel(g);

        g.drawString(font, title, panelX + 18, panelY + 14, ChannelUiTheme.CYAN, false);
        g.drawString(font, Component.translatable("tabchannel.gui.create.subtitle"), panelX + 18, panelY + 28, ChannelUiTheme.GRAY, false);

        g.drawString(font, Component.translatable("tabchannel.gui.create.name"), panelX + 24, panelY + 46, ChannelUiTheme.WHITE, false);
        g.drawString(font, Component.translatable("tabchannel.gui.create.type", type), panelX + 24, panelY + 122, ChannelUiTheme.GREEN, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics g) {
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xD8071018);

        int c = 0xFF00E5FF;
        g.fill(panelX, panelY, panelX + panelW, panelY + 2, c);
        g.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, c);
        g.fill(panelX, panelY, panelX + 2, panelY + panelH, c);
        g.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, c);
    }

    private void createChannel() {
        String name = nameBox.getValue().trim();

        if (name.isBlank()) {
            return;
        }

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand("channelcreate \"" + name + "\" " + type);
        }

        minecraft.setScreen(parent);
    }
}