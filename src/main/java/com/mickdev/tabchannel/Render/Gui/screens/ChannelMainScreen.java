package com.mickdev.tabchannel.Render.Gui.screens;


import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelSelectTabPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import com.mickdev.tabchannel.Render.Hud.MpHudInteraction;
import com.mickdev.tabchannel.Render.Gui.ClientChannelNotifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.util.List;

public final class ChannelMainScreen extends Screen {

    private int panelX, panelY, panelW, panelH;
    private int listX, listY, listW, listH;
    private int rowH = 24;

    private int scrollIndex = 0;
    private boolean draggingScrollbar = false;

    public ChannelMainScreen() {
        super(Component.translatable("tabchannel.gui.main.title"));
    }

    @Override
    protected void init() {
        panelW = Math.min(720, this.width - 40);
        panelH = Math.min(315, this.height - 40);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        listX = panelX + 24;
        listY = panelY + 64;
        listW = panelW - 260;
        listH = panelH - 92;
        rowH = 24;

        int bx = panelX + panelW - 220;
        int by = panelY + 78;

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.create"),
                        b -> minecraft.setScreen(new ChannelCreateScreen(this))
                )
                .bounds(bx, by, 190, 24)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.permissions"),
                        b -> minecraft.setScreen(new ChannelPermissionsScreen(this))
                )
                .bounds(bx, by + 34, 190, 24)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.private_messages"),
                        b -> MpHudInteraction.openMpGui(minecraft)
                )
                .bounds(bx, by + 68, 190, 24)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.close"),
                        b -> onClose()
                )
                .bounds(bx, panelY + panelH - 42, 190, 24)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Pas de blur.
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        clampScroll();
        renderPanel(g);

        g.drawString(font, title, panelX + 24, panelY + 18, ChannelUiTheme.CYAN, false);
        g.drawString(font, Component.translatable("tabchannel.gui.main.subtitle"), panelX + 24, panelY + 40, ChannelUiTheme.GRAY, false);

        renderChannelList(g, mouseX, mouseY);
        renderScrollbar(g);
        renderRightInfo(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics g) {
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xD8071018);
        g.fill(listX - 8, listY - 8, listX + listW + 14, listY + listH + 8, 0xB8040A12);

        int rightX = panelX + panelW - 240;
        g.fill(rightX, panelY + 60, panelX + panelW - 18, panelY + panelH - 18, 0xCC050A12);

        int c = 0xFF00E5FF;
        g.fill(panelX, panelY, panelX + panelW, panelY + 2, c);
        g.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, c);
        g.fill(panelX, panelY, panelX + 2, panelY + panelH, c);
        g.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, c);
    }

    private void renderChannelList(GuiGraphics g, int mouseX, int mouseY) {
        List<ClientChannelTabState.TabEntry> tabs = ClientChannelTabState.getTabs();

        if (tabs.isEmpty()) {
            g.drawString(font, Component.translatable("tabchannel.gui.main.no_tabs"), listX, listY, ChannelUiTheme.GRAY, false);
            return;
        }

        int visibleRows = getVisibleRows();
        int end = Math.min(tabs.size(), scrollIndex + visibleRows);

        for (int i = scrollIndex; i < end; i++) {
            ClientChannelTabState.TabEntry tab = tabs.get(i);
            int visibleI = i - scrollIndex;
            int y = listY + visibleI * (rowH + 4);

            boolean hover = mouseX >= listX && mouseX <= listX + listW && mouseY >= y && mouseY <= y + rowH;
            boolean selected = tab.selected();

            int bg = selected ? 0xAA063B46 : hover ? 0x880B2A35 : 0x66060D16;
            int border = selected ? 0xFF00E5FF : hover ? 0xAA00E5FF : 0x5500E5FF;

            g.fill(listX, y, listX + listW, y + rowH, bg);
            g.fill(listX, y, listX + listW, y + 1, border);
            g.fill(listX, y + rowH - 1, listX + listW, y + rowH, border);
            g.fill(listX, y, listX + 1, y + rowH, border);
            g.fill(listX + listW - 1, y, listX + listW, y + rowH, border);

            int unread = ClientChannelNotifications.getUnread(tab.id());
            int nameColor = getTabTextColor(tab, selected);

            g.drawString(font, tab.displayName(), listX + 10, y + 4, nameColor, false);
            g.drawString(font, tab.global() ? "GLOBAL" : tab.id(), listX + 10, y + 15, ChannelUiTheme.GRAY, false);

            if (unread > 0) {
                String badge = String.valueOf(Math.min(99, unread));
                int badgeX = listX + listW - 28;
                g.fill(badgeX, y + 5, badgeX + 20, y + 19, 0xDDEF4444);
                ChannelUiTheme.centered(g, Component.literal(badge), badgeX, y + 8, 20, ChannelUiTheme.WHITE);
            }
        }
    }

    private int getTabTextColor(ClientChannelTabState.TabEntry tab, boolean selected) {
        String color = tab.tabColor();

        if ("RED".equalsIgnoreCase(color)) return 0xFFFF5555;
        if ("BLUE".equalsIgnoreCase(color)) return 0xFF5555FF;
        if ("CYAN".equalsIgnoreCase(color)) return 0xFF00E5FF;
        if ("GREEN".equalsIgnoreCase(color)) return 0xFF55FF55;
        if ("YELLOW".equalsIgnoreCase(color)) return 0xFFFFFF55;
        if ("GOLD".equalsIgnoreCase(color)) return 0xFFFFAA00;
        if ("PURPLE".equalsIgnoreCase(color)) return 0xFFFF55FF;
        if ("WHITE".equalsIgnoreCase(color)) return 0xFFFFFFFF;
        if ("GRAY".equalsIgnoreCase(color)) return selected ? 0xFF55FF55 : 0xFFAAAAAA;

        return selected ? ChannelUiTheme.GREEN : ChannelUiTheme.WHITE;
    }

    private void renderScrollbar(GuiGraphics g) {
        List<ClientChannelTabState.TabEntry> tabs = ClientChannelTabState.getTabs();
        int visibleRows = getVisibleRows();

        if (tabs.size() <= visibleRows) {
            return;
        }

        int barX = listX + listW + 6;
        int barY = listY;
        int barW = 6;
        int barH = listH;

        g.fill(barX, barY, barX + barW, barY + barH, 0x66000000);

        int maxScroll = Math.max(1, tabs.size() - visibleRows);
        int thumbH = Math.max(22, (int) ((visibleRows / (float) tabs.size()) * barH));
        int thumbY = barY + (int) ((scrollIndex / (float) maxScroll) * (barH - thumbH));

        g.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF00E5FF);
    }

    private void renderRightInfo(GuiGraphics g) {
        String selected = ClientChannelTabState.getSelectedChannelId();

        int x = panelX + panelW - 220;
        int y = panelY + 150;

        g.drawString(font, Component.translatable("tabchannel.gui.main.selected_title"), x, y, ChannelUiTheme.CYAN, false);
        g.drawString(font, Component.literal(selected == null || selected.isBlank() ? "global" : selected), x, y + 16, ChannelUiTheme.WHITE, false);

        g.drawString(font, Component.translatable("tabchannel.gui.main.mention_tip"), x, y + 48, ChannelUiTheme.GRAY, false);
       // g.drawString(font, Component.literal("@player"), x, y + 64, ChannelUiTheme.GREEN, false);
        //g.drawString(font, Component.literal("@staff / @admin"), x, y + 80, ChannelUiTheme.GREEN, false);
    }

    private int getVisibleRows() {
        return Math.max(1, listH / (rowH + 4));
    }

    private void clampScroll() {
        int size = ClientChannelTabState.getTabs().size();
        int maxScroll = Math.max(0, size - getVisibleRows());

        scrollIndex = Math.max(0, Math.min(scrollIndex, maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listX && mouseX <= listX + listW + 16 && mouseY >= listY && mouseY <= listY + listH) {
            scrollIndex -= (int) Math.signum(scrollY);
            clampScroll();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ClientChannelTabState.TabEntry> tabs = ClientChannelTabState.getTabs();

        int visibleRows = getVisibleRows();
        int end = Math.min(tabs.size(), scrollIndex + visibleRows);

        for (int i = scrollIndex; i < end; i++) {
            ClientChannelTabState.TabEntry tab = tabs.get(i);
            int visibleI = i - scrollIndex;
            int y = listY + visibleI * (rowH + 4);

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= y && mouseY <= y + rowH) {
                ClientChannelTabState.setSelectedChannelId(tab.id());
                ClientChannelChatState.resetScroll(tab.id());
                ClientChannelNotifications.clearUnread(tab.id());
                ClientPlayNetworking.send(new ChannelSelectTabPayload(tab.id()));
                return true;
            }
        }

        if (isOverScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            updateScrollFromMouse(mouseY);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= listX + listW + 4
                && mouseX <= listX + listW + 14
                && mouseY >= listY
                && mouseY <= listY + listH;
    }

    private void updateScrollFromMouse(double mouseY) {
        List<ClientChannelTabState.TabEntry> tabs = ClientChannelTabState.getTabs();
        int visibleRows = getVisibleRows();
        int maxScroll = Math.max(0, tabs.size() - visibleRows);

        if (maxScroll <= 0) {
            scrollIndex = 0;
            return;
        }

        double ratio = (mouseY - listY) / (double) listH;
        ratio = Math.max(0.0, Math.min(1.0, ratio));

        scrollIndex = (int) Math.round(ratio * maxScroll);
        clampScroll();
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new ChannelMainScreen());
    }
}