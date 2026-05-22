package com.mickdev.tabchannel.Render.Gui.screens;

import com.mickdev.tabchannel.Common.Mp.ClientMpAccess;
import com.mickdev.tabchannel.Common.Mp.ClientMpChatStore;
import com.mickdev.tabchannel.Common.Mp.ClientMpNotifications;
import com.mickdev.tabchannel.Common.Mp.ClientMpPersistence;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpClientNetworking;
import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MpChatScreen extends Screen {

    private record PeerRow(String key, String displayName, boolean online) {
    }

    private static final int ROW_H = 26;
    private static final int BUBBLE_H = 18;
    private static final int BUBBLE_GAP = 6;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private int listContentW;
    private int chatX;
    private int chatY;
    private int chatW;
    private int chatH;

    private String selectedPlayerKey = "";
    private final List<PeerRow> peerRows = new ArrayList<>();
    private EditBox messageInput;
    private EditBox searchInput;

    private int listScrollIndex;
    private int chatScrollIndex;
    private boolean draggingListScrollbar;
    private boolean draggingChatScrollbar;
    private boolean searchMode;
    private List<ClientMpChatStore.SearchHit> searchResults = List.of();

    public MpChatScreen() {
        super(Component.translatable("tabchannel.mp.gui.title"));
    }

    public static void open() {
        ClientMpPersistence.loadForCurrentServer();
        Minecraft.getInstance().setScreen(new MpChatScreen());
    }

    @Override
    protected void init() {
        ClientMpNotifications.setScreenOpen(true);

        panelW = Math.min(780, width - 40);
        panelH = Math.min(440, height - 40);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        listX = panelX + 20;
        listY = panelY + 78;
        listW = 220;
        listContentW = listW - 12;
        listH = panelH - 120;

        chatX = listX + listW + 16;
        chatY = panelY + 78;
        chatW = panelW - listW - 56;
        chatH = panelH - 120;

        int bottomY = panelY + panelH - 36;
        int bottomControlH = 22;
        int bottomGap = 8;
        int inputH = 20;
        int actionBtnW = 90;
        int sendW = 96;
        int refreshX = panelX + 20;
        int closeX = refreshX + actionBtnW + bottomGap;
        int sendX = panelX + panelW - 20 - sendW;
        int inputX = closeX + actionBtnW + bottomGap;
        int inputW = Math.max(80, sendX - bottomGap - inputX);
        int inputY = bottomY + (bottomControlH - inputH) / 2;

        rebuildPeerRows();
        autoSelectPeer();

        searchInput = new EditBox(font, listX, panelY + 52, panelW - 40, 18, Component.empty());
        searchInput.setMaxLength(128);
        searchInput.setBordered(true);
        searchInput.setHint(Component.translatable("tabchannel.mp.gui.search_hint"));
        searchInput.setResponder(query -> {
            rebuildPeerRows();
            searchMode = false;
        });
        addRenderableWidget(searchInput);

        messageInput = new EditBox(font, inputX, inputY, inputW, inputH, Component.empty());
        messageInput.setMaxLength(256);
        messageInput.setBordered(true);
        messageInput.setTextColor(0xFFFFFF);
        messageInput.setHint(Component.translatable("tabchannel.mp.gui.input_hint"));
        addRenderableWidget(messageInput);

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.mp.gui.send"), b -> sendMessage())
                .bounds(sendX, bottomY, sendW, bottomControlH)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.mp.gui.refresh"), b -> {
                    rebuildPeerRows();
                    init(minecraft, width, height);
                })
                .bounds(refreshX, bottomY, actionBtnW, bottomControlH)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("tabchannel.mp.gui.close"), b -> onClose())
                .bounds(closeX, bottomY, actionBtnW, bottomControlH)
                .build());

        setInitialFocus(messageInput);
    }

    @Override
    public void onClose() {
        ClientMpNotifications.setScreenOpen(false);
        ClientMpNotifications.setActivePeer(null);
        ClientMpPersistence.saveForCurrentServer();
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderPanel(g);
        renderPlayerList(g, mouseX, mouseY);
        renderListScrollbar(g);
        renderChatArea(g);
        renderChatScrollbar(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics g) {
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xD8071018);
        g.fill(listX - 6, listY - 6, listX + listW + 6, listY + listH + 6, 0xB8040A12);
        g.fill(chatX - 6, chatY - 6, chatX + chatW + 6, chatY + chatH + 6, 0xCC050A12);

        int accent = 0xFF00E5FF;
        g.fill(panelX, panelY, panelX + panelW, panelY + 2, accent);
        g.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, accent);

        g.drawString(font, title, panelX + 20, panelY + 16, ChannelUiTheme.CYAN, false);

        long onlineCount = peerRows.stream().filter(PeerRow::online).count();
        g.drawString(font,
                Component.translatable("tabchannel.mp.gui.online", onlineCount),
                listX,
                panelY + 38,
                ChannelUiTheme.GRAY,
                false);

        if (!selectedPlayerKey.isBlank() && !searchMode) {
            g.drawString(font,
                    Component.translatable("tabchannel.mp.gui.chat_with", ClientMpChatStore.getDisplayName(selectedPlayerKey)),
                    chatX,
                    panelY + 38,
                    ChannelUiTheme.WHITE,
                    false);
        } else if (searchMode) {
            g.drawString(font,
                    Component.translatable("tabchannel.mp.gui.search_results", searchResults.size()),
                    chatX,
                    panelY + 38,
                    ChannelUiTheme.GOLD,
                    false);
        }
    }

    private void renderPlayerList(GuiGraphics g, int mouseX, int mouseY) {
        int visibleRows = getListVisibleRows();
        int end = Math.min(peerRows.size(), listScrollIndex + visibleRows);

        for (int i = listScrollIndex; i < end; i++) {
            PeerRow peer = peerRows.get(i);
            int visibleI = i - listScrollIndex;
            int y = listY + visibleI * (ROW_H + 2);

            boolean hover = mouseX >= listX && mouseX <= listX + listContentW
                    && mouseY >= y && mouseY <= y + ROW_H;
            boolean selected = peer.key().equals(selectedPlayerKey);
            int bg = selected ? 0xAA063B46 : hover ? 0x880B2A35 : 0x66060D16;
            int border = selected ? 0xFF00E5FF : hover ? 0xAA00E5FF : 0x5500E5FF;

            g.fill(listX, y, listX + listContentW, y + ROW_H, bg);
            g.fill(listX, y, listX + listContentW, y + 1, border);
            g.fill(listX, y + ROW_H - 1, listX + listContentW, y + ROW_H, border);

            int nameColor = peer.online() ? ChannelUiTheme.WHITE : ChannelUiTheme.GRAY;
            g.drawString(font, peer.displayName(), listX + 8, y + 5, nameColor, false);

            if (!peer.online()) {
                g.drawString(font,
                        Component.translatable("tabchannel.mp.gui.offline_tag"),
                        listX + 8,
                        y + 14,
                        0xFF888888,
                        false);
            }

            int unread = ClientMpNotifications.getUnread(peer.key());

            if (unread > 0) {
                drawUnreadBadge(g, listX + listContentW - 22, y + 5, unread);
            }
        }
    }

    private void drawUnreadBadge(GuiGraphics g, int x, int y, int unread) {
        String badge = unread > 99 ? "99+" : String.valueOf(unread);
        int size = 18;
        g.fill(x, y, x + size, y + size, 0xFFEF4444);
        g.fill(x, y, x + size, y + 1, 0xFFFF8888);
        g.fill(x, y + size - 1, x + size, y + size, 0xFFAA2222);
        ChannelUiTheme.centered(g, Component.literal(badge), x, y + 5, size, ChannelUiTheme.WHITE);
    }

    private void renderListScrollbar(GuiGraphics g) {
        int visibleRows = getListVisibleRows();
        if (peerRows.size() <= visibleRows) {
            return;
        }

        int barX = listX + listContentW + 4;
        int barY = listY;
        int barW = 6;
        int barH = listH;

        g.fill(barX, barY, barX + barW, barY + barH, 0x66000000);

        int maxScroll = Math.max(1, peerRows.size() - visibleRows);
        int thumbH = Math.max(22, (int) ((visibleRows / (float) peerRows.size()) * barH));
        int thumbY = barY + (int) ((listScrollIndex / (float) maxScroll) * (barH - thumbH));

        g.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF00E5FF);
    }

    private void renderChatArea(GuiGraphics g) {
        if (searchMode) {
            renderSearchResults(g);
            return;
        }

        if (selectedPlayerKey.isBlank()) {
            g.drawString(font,
                    Component.translatable("tabchannel.mp.gui.select_player"),
                    chatX + 12,
                    chatY + 12,
                    ChannelUiTheme.GRAY,
                    false);
            return;
        }

        List<ClientMpChatStore.MpLine> lines = ClientMpChatStore.getMessages(selectedPlayerKey);
        int visibleRows = getChatVisibleRows();
        int start = Math.max(0, lines.size() - visibleRows - chatScrollIndex);
        int end = Math.min(lines.size(), start + visibleRows);
        int y = chatY + 8;

        for (int i = start; i < end; i++) {
            ClientMpChatStore.MpLine line = lines.get(i);
            String text = line.text();
            int textW = font.width(text);
            int bubbleW = Math.min(chatW - 40, textW + 16);

            if (line.incoming()) {
                int bx = chatX + 8;
                g.fill(bx, y, bx + bubbleW, y + BUBBLE_H, 0xCC1D4ED8);
                g.drawString(font, text, bx + 8, y + 5, ChannelUiTheme.WHITE, false);
            } else {
                int bx = chatX + chatW - bubbleW - 8;
                g.fill(bx, y, bx + bubbleW, y + BUBBLE_H, 0xCC166534);
                g.drawString(font, text, bx + 8, y + 5, ChannelUiTheme.WHITE, false);
            }

            y += BUBBLE_H + BUBBLE_GAP;
        }
    }

    private void renderSearchResults(GuiGraphics g) {
        int y = chatY + 8;
        int maxY = chatY + chatH - 44;
        int visible = getChatVisibleRows();
        int start = Math.max(0, searchResults.size() - visible - chatScrollIndex);
        int end = Math.min(searchResults.size(), start + visible);

        for (int i = start; i < end; i++) {
            if (y > maxY) {
                break;
            }

            ClientMpChatStore.SearchHit hit = searchResults.get(i);
            String line = "[" + hit.peerDisplayName() + "] " + hit.line().text();
            g.drawString(font, line, chatX + 8, y, ChannelUiTheme.WHITE, false);
            y += 14;
        }
    }

    private void renderChatScrollbar(GuiGraphics g) {
        int total = searchMode ? searchResults.size() : ClientMpChatStore.getMessages(selectedPlayerKey).size();
        int visible = getChatVisibleRows();

        if (total <= visible) {
            return;
        }

        int barX = chatX + chatW - 8;
        int barY = chatY;
        int barW = 6;
        int barH = chatH - 36;

        g.fill(barX, barY, barX + barW, barY + barH, 0x66000000);

        int maxScroll = Math.max(1, total - visible);
        int thumbH = Math.max(22, (int) ((visible / (float) total) * barH));
        int thumbY = barY + (int) ((chatScrollIndex / (float) maxScroll) * (barH - thumbH));

        g.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF00E5FF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int visibleRows = getListVisibleRows();
        int end = Math.min(peerRows.size(), listScrollIndex + visibleRows);

        for (int i = listScrollIndex; i < end; i++) {
            PeerRow peer = peerRows.get(i);
            int visibleI = i - listScrollIndex;
            int y = listY + visibleI * (ROW_H + 2);

            if (mouseX >= listX && mouseX <= listX + listContentW && mouseY >= y && mouseY <= y + ROW_H) {
                selectPlayer(peer.key());
                searchMode = false;
                return true;
            }
        }

        if (isOverListScrollbar(mouseX, mouseY)) {
            draggingListScrollbar = true;
            updateListScrollFromMouse(mouseY);
            return true;
        }

        if (isOverChatScrollbar(mouseX, mouseY)) {
            draggingChatScrollbar = true;
            updateChatScrollFromMouse(mouseY);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingListScrollbar) {
            updateListScrollFromMouse(mouseY);
            return true;
        }

        if (draggingChatScrollbar) {
            updateChatScrollFromMouse(mouseY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingListScrollbar = false;
        draggingChatScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            listScrollIndex -= (int) Math.signum(scrollY);
            clampListScroll();
            return true;
        }

        if (mouseX >= chatX && mouseX <= chatX + chatW && mouseY >= chatY && mouseY <= chatY + chatH) {
            chatScrollIndex -= (int) Math.signum(scrollY);
            clampChatScroll();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void focusPlayer(String key) {
        selectedPlayerKey = key;
        searchMode = false;
        chatScrollIndex = 0;
        ClientMpNotifications.setActivePeer(ClientMpChatStore.getDisplayName(key));
    }

    private void selectPlayer(String key) {
        focusPlayer(key);
        ClientMpNotifications.clearUnread(key);
    }

    private void rebuildPeerRows() {
        peerRows.clear();

        Minecraft mc = Minecraft.getInstance();
        Set<String> keys = new HashSet<>(ClientMpChatStore.getAllPeerKeys());
        Set<String> onlineKeys = new HashSet<>();

        if (ClientMpAccess.browsesAllOnlinePlayers()
                && mc.player != null
                && mc.player.connection != null) {
            String self = mc.player.getGameProfile().getName();

            for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                if (info == null || info.getProfile() == null) {
                    continue;
                }

                String name = info.getProfile().getName();

                if (name != null && !name.isBlank() && !name.equalsIgnoreCase(self)) {
                    String key = ClientMpChatStore.normalize(name);
                    keys.add(key);
                    onlineKeys.add(key);
                }
            }
        }

        String filter = searchInput == null ? "" : searchInput.getValue().trim().toLowerCase(Locale.ROOT);

        for (String key : keys) {
            String display = ClientMpChatStore.getDisplayName(key);
            boolean online = onlineKeys.contains(key);

            if (!filter.isBlank()) {
                boolean nameMatch = display.toLowerCase(Locale.ROOT).contains(filter);
                boolean msgMatch = !ClientMpChatStore.searchMessages(filter, 1).stream()
                        .filter(hit -> hit.peerKey().equals(key))
                        .toList()
                        .isEmpty();

                if (!nameMatch && !msgMatch) {
                    continue;
                }
            }

            peerRows.add(new PeerRow(key, display, online));
        }

        peerRows.sort(Comparator
                .comparingInt((PeerRow row) -> -ClientMpNotifications.getUnread(row.key()))
                .thenComparing((PeerRow row) -> !row.online())
                .thenComparing(PeerRow::displayName, String.CASE_INSENSITIVE_ORDER));

        clampListScroll();
    }

    private void autoSelectPeer() {
        if (!selectedPlayerKey.isBlank()
                && peerRows.stream().noneMatch(row -> row.key().equals(selectedPlayerKey))) {
            selectedPlayerKey = "";
        }
    }

    private void sendMessage() {
        if (messageInput == null || selectedPlayerKey.isBlank()) {
            return;
        }

        String text = messageInput.getValue().trim();

        if (text.isEmpty()) {
            return;
        }

        String target = ClientMpChatStore.getDisplayName(selectedPlayerKey);
        MpClientNetworking.send(target, text);
        ClientMpChatStore.addMessage(target, text, false);
        messageInput.setValue("");
        chatScrollIndex = 0;
    }

    private int getListVisibleRows() {
        return Math.max(1, listH / (ROW_H + 2));
    }

    private int getChatVisibleRows() {
        return Math.max(1, (chatH - 44) / (BUBBLE_H + BUBBLE_GAP));
    }

    private void clampListScroll() {
        int maxScroll = Math.max(0, peerRows.size() - getListVisibleRows());
        listScrollIndex = Math.max(0, Math.min(listScrollIndex, maxScroll));
    }

    private void clampChatScroll() {
        int total = searchMode ? searchResults.size() : ClientMpChatStore.getMessages(selectedPlayerKey).size();
        int maxScroll = Math.max(0, total - getChatVisibleRows());
        chatScrollIndex = Math.max(0, Math.min(chatScrollIndex, maxScroll));
    }

    private boolean isOverListScrollbar(double mouseX, double mouseY) {
        return mouseX >= listX + listContentW + 2
                && mouseX <= listX + listW + 4
                && mouseY >= listY
                && mouseY <= listY + listH;
    }

    private boolean isOverChatScrollbar(double mouseX, double mouseY) {
        return mouseX >= chatX + chatW - 10
                && mouseX <= chatX + chatW
                && mouseY >= chatY
                && mouseY <= chatY + chatH - 36;
    }

    private void updateListScrollFromMouse(double mouseY) {
        int visibleRows = getListVisibleRows();
        int maxScroll = Math.max(0, peerRows.size() - visibleRows);

        if (maxScroll <= 0) {
            listScrollIndex = 0;
            return;
        }

        double ratio = (mouseY - listY) / (double) listH;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        listScrollIndex = (int) Math.round(ratio * maxScroll);
        clampListScroll();
    }

    private void updateChatScrollFromMouse(double mouseY) {
        int visible = getChatVisibleRows();
        int total = searchMode ? searchResults.size() : ClientMpChatStore.getMessages(selectedPlayerKey).size();
        int maxScroll = Math.max(0, total - visible);

        if (maxScroll <= 0) {
            chatScrollIndex = 0;
            return;
        }

        double ratio = (mouseY - chatY) / (double) (chatH - 36);
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        chatScrollIndex = (int) Math.round(ratio * maxScroll);
        clampChatScroll();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (messageInput != null && messageInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
