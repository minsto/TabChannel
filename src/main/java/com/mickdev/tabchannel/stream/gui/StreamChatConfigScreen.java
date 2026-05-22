package com.mickdev.tabchannel.stream.gui;

import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import com.mickdev.tabchannel.stream.StreamChatManager;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.config.StreamOverlayLayoutConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class StreamChatConfigScreen extends Screen {

    private static final int PANEL_W = 400;
    private static final int FIELD_H = 18;
    private static final int BLOCK_H = 36;
    private static final int FOOTER_H = 56;

    private EditBox twitchChannel;
    private EditBox twitchLogin;
    private EditBox twitchToken;
    private EditBox messageLimit;

    private int panelX;
    private int panelY;
    private int contentLeft;
    private int fieldW;
    private int formTop;
    private int footerY1;
    private int footerY2;

    private int labelYChannel;
    private int fieldYChannel;
    private int labelYLogin;
    private int fieldYLogin;
    private int labelYToken;
    private int fieldYToken;
    private int labelYLimit;
    private int fieldYLimit;

    private final List<AbstractWidget> formWidgets = new ArrayList<>();

    public StreamChatConfigScreen() {
        super(Component.translatable("tabchannel.stream.config.title"));
    }

    public static void open() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new StreamChatConfigScreen());
    }

    @Override
    protected void init() {
        clearWidgets();
        formWidgets.clear();

        panelX = width / 2 - PANEL_W / 2;
        panelY = 8;
        contentLeft = panelX + 14;
        fieldW = PANEL_W - 28;
        formTop = panelY + 54;
        footerY1 = height - FOOTER_H;
        footerY2 = height - 28;

        int halfW = (fieldW - 8) / 2;
        int y = panelY + 28;

        addRenderableWidget(CycleButton.onOffBuilder(StreamChatConfig.INSTANCE.integrationEnabled)
                .create(contentLeft, y, halfW, 20, Component.translatable("tabchannel.stream.config.integration"), (b, v) ->
                        StreamChatConfig.INSTANCE.integrationEnabled = v));
        addRenderableWidget(CycleButton.onOffBuilder(StreamOverlayLayoutConfig.visible())
                .create(contentLeft + halfW + 8, y, halfW, 20, Component.translatable("tabchannel.stream.config.overlay_visible"), (b, v) ->
                        StreamOverlayLayoutConfig.setVisible(v)));

        layoutFormFields();
        layoutFooterButtons();
    }

    private void layoutFormFields() {
        int y = formTop + 6;

        labelYChannel = y;
        fieldYChannel = y + 11;
        twitchChannel = addFormField(fieldYChannel, StreamChatConfig.INSTANCE.twitchChannel);
        y += BLOCK_H;

        labelYLogin = y;
        fieldYLogin = y + 11;
        twitchLogin = addFormField(fieldYLogin, StreamChatConfig.INSTANCE.twitchLogin);
        y += BLOCK_H;

        labelYToken = y;
        fieldYToken = y + 11;
        twitchToken = addFormField(fieldYToken, StreamChatConfig.INSTANCE.twitchToken);
        twitchToken.setFormatter((text, cursorPos) -> net.minecraft.util.FormattedCharSequence.forward("*".repeat(text.length()), net.minecraft.network.chat.Style.EMPTY));
        y += BLOCK_H;

        labelYLimit = y;
        fieldYLimit = y + 7;
        messageLimit = new EditBox(font, contentLeft + 140, fieldYLimit, 52, FIELD_H, Component.literal("limit"));
        messageLimit.setValue(String.valueOf(StreamChatConfig.INSTANCE.messageLimit));
        messageLimit.setBordered(true);
        messageLimit.setTextColor(ChannelUiTheme.WHITE);
        addFormWidget(messageLimit);
    }

    private EditBox addFormField(int y, String value) {
        EditBox box = new EditBox(font, contentLeft, y, fieldW, FIELD_H, Component.empty());
        box.setValue(value);
        box.setBordered(true);
        box.setTextColor(ChannelUiTheme.WHITE);
        addFormWidget(box);
        return box;
    }

    private void addFormWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
        formWidgets.add(widget);
    }

    private void layoutFooterButtons() {
        int left = width / 2 - 198;
        int w = 94;
        int gap = 4;

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.config.connect_twitch"), b -> {
                    persistFields();
                    StreamChatConfig.save();
                    StreamChatManager.connectTwitch();
                })
                .bounds(left, footerY1, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.config.disconnect"), b ->
                        StreamChatManager.disconnectTwitch())
                .bounds(left + w + gap, footerY2, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.config.test"), b -> StreamChatManager.addTestBatch())
                .bounds(left + (w + gap) * 2, footerY2, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.config.save"), b -> saveAndClose())
                .bounds(left + (w + gap) * 3, footerY1, w, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.stream.config.clear"), b -> StreamChatManager.clearMessages())
                .bounds(left, footerY2, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("tabchannel.gui.button.close"), b -> onClose())
                .bounds(left + (w + gap) * 3, footerY2, w, 20).build());
    }

    private void persistFields() {
        if (twitchChannel != null) {
            StreamChatConfig.INSTANCE.twitchChannel = twitchChannel.getValue().trim();
        }
        if (twitchLogin != null) {
            StreamChatConfig.INSTANCE.twitchLogin = twitchLogin.getValue().trim();
        }
        if (twitchToken != null) {
            StreamChatConfig.INSTANCE.twitchToken = twitchToken.getValue().trim();
        }
        if (messageLimit != null) {
            try {
                StreamChatConfig.INSTANCE.messageLimit = Integer.parseInt(messageLimit.getValue().trim());
            } catch (NumberFormatException ignored) {
                StreamChatConfig.INSTANCE.messageLimit = 50;
            }
        }
    }

    private void saveAndClose() {
        persistFields();
        StreamChatConfig.INSTANCE.overlayOpacity = StreamOverlayLayoutConfig.opacity();
        StreamChatConfig.save();

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("tabchannel.stream.config.saved"), false);
        }

        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int panelH = height - panelY - 4;
        ChannelUiTheme.panel(g, panelX, panelY, PANEL_W, panelH);
        g.fill(panelX + 8, footerY1 - 6, panelX + PANEL_W - 8, footerY1 - 4, ChannelUiTheme.CYAN_SOFT);
        g.fill(contentLeft - 2, formTop, contentLeft + fieldW + 2, footerY1 - 10, 0xAA0F1419);

        super.render(g, mouseX, mouseY, partialTick);
        drawHeader(g);
        drawFieldLabels(g);
    }

    private void drawHeader(GuiGraphics g) {
        int cx = width / 2;
        g.drawCenteredString(font, title, cx, panelY + 6, ChannelUiTheme.CYAN);

        int barY = panelY + 20;
        g.fill(panelX + 10, barY, panelX + PANEL_W - 10, barY + 14, 0xCC0A0A12);
        ChannelUiTheme.border(g, panelX + 10, barY, PANEL_W - 20, 14, ChannelUiTheme.CYAN_SOFT);

        Component status = resolveStatus();
        int statusColor = StreamChatConfig.INSTANCE.integrationEnabled ? ChannelUiTheme.GREEN : ChannelUiTheme.RED;
        ChannelUiTheme.centered(g, status, panelX + 10, barY + 3, PANEL_W - 20, statusColor);

        g.drawCenteredString(font, Component.translatable("tabchannel.stream.config.hint.twitch"), cx, formTop - 2, ChannelUiTheme.GRAY);
    }

    private Component resolveStatus() {
        if (!StreamChatConfig.INSTANCE.integrationEnabled) {
            return Component.translatable("tabchannel.stream.status.integration_off");
        }

        if (StreamChatManager.isTwitchConnected()) {
            return Component.translatable("tabchannel.stream.status.connected_twitch");
        }

        Component live = StreamChatManager.statusComponent();
        if (live != null && !live.getString().isBlank()) {
            return sanitizeStatus(live);
        }

        return Component.translatable("tabchannel.stream.status.ready");
    }

    private Component sanitizeStatus(Component raw) {
        String text = raw.getString();
        if (text.contains("Cannot invoke") || text.contains("java.") || text.contains("StreamChatProvider")) {
            return Component.translatable("tabchannel.stream.status.internal_error");
        }
        if (text.length() > 52) {
            return Component.literal(text.substring(0, 49) + "...");
        }
        return raw;
    }

    private void drawFieldLabels(GuiGraphics g) {
        int lx = contentLeft;
        if (labelYChannel >= 0) {
            drawLabel(g, "tabchannel.stream.config.label.twitch_channel", lx, labelYChannel);
        }
        if (labelYLogin >= 0) {
            drawLabel(g, "tabchannel.stream.config.label.twitch_login", lx, labelYLogin);
        }
        if (labelYToken >= 0) {
            drawLabel(g, "tabchannel.stream.config.label.twitch_token", lx, labelYToken);
        }
        if (labelYLimit >= 0) {
            g.drawString(font, Component.translatable("tabchannel.stream.config.label.message_limit"), lx, labelYLimit, ChannelUiTheme.GRAY, false);
        }
    }

    private void drawLabel(GuiGraphics g, String key, int x, int y) {
        g.drawString(font, Component.translatable(key), x, y, ChannelUiTheme.WHITE, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
