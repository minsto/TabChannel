package com.mickdev.tabchannel.Render.Gui;



import com.mickdev.tabchannel.ChatManager;
import com.mickdev.tabchannel.ChannelTabColor;
import com.mickdev.tabchannel.ChannelTabLayout;
import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelChangePagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelSelectTabPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.TabChannel;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChannelTabRenderer {

    private static final ResourceLocation TAB_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    TabChannel.MODID,
                    "textures/gui/chat_tab.png"
            );

    private ChannelTabRenderer() {
    }

    private static List<ClientChannelTabState.TabEntry> visibleTabsForCurrentPage() {
        List<ClientChannelTabState.TabEntry> all = new ArrayList<>(ClientChannelTabState.getTabs());

        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        int pageSize = ChatManager.MAX_VISIBLE_TABS;
        int page = ClientChannelTabState.getPage();
        int maxPage = Math.max(0, (all.size() - 1) / pageSize);
        int safePage = Math.min(Math.max(0, page), maxPage);
        int start = safePage * pageSize;
        int end = Math.min(all.size(), start + pageSize);

        return all.subList(start, end);
    }

    private static String pageIndicatorText(List<ClientChannelTabState.TabEntry> allTabs) {
        int pageSize = ChatManager.MAX_VISIBLE_TABS;
        int pageCount = Math.max(1, (allTabs.size() + pageSize - 1) / pageSize);
        int displayPage = Math.min(ClientChannelTabState.getPage() + 1, pageCount);

        return "< Page " + displayPage + " / " + pageCount + " >";
    }

    public static void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen instanceof ChatScreen)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        int startX = ChannelHudLayoutConfig.chatX();
        int x = startX;
        int y = ChannelHudLayoutConfig.chatY() - 24;

        List<ClientChannelTabState.TabEntry> visible = visibleTabsForCurrentPage();

        for (ClientChannelTabState.TabEntry tab : visible) {
            int width = ChannelTabLayout.computeWidth(tab.displayName());

            guiGraphics.blit(
                    TAB_TEXTURE,
                    x,
                    y,
                    0,
                    0,
                    width,
                    ChannelTabLayout.TAB_HEIGHT,
                    width,
                    ChannelTabLayout.TAB_HEIGHT
            );

            int textWidth = mc.font.width(tab.displayName());
            int textX = x + (width - textWidth) / 2;
            int textY = y + 6;
            int unread = ClientChannelNotifications.getUnread(tab.id());
            int color = ChannelTabColor.labelColor(tab.tabColor(), tab.selected(), unread > 0);

            guiGraphics.drawString(mc.font, tab.displayName(), textX, textY, color, false);

            if (unread > 0) {
                String badge = String.valueOf(Math.min(99, unread));
                int bx = x + width - 14;

                guiGraphics.fill(bx, y + 2, bx + 12, y + 12, 0xCCFF4444);
                guiGraphics.drawString(mc.font, badge, bx + 3, y + 3, 0xFFFFFF, false);
            }

            x += width + 2;
        }

        String pageText = pageIndicatorText(ClientChannelTabState.getTabs());

        guiGraphics.drawString(
                mc.font,
                Component.literal(pageText),
                startX,
                y - 12,
                0xFFFFFF,
                false
        );
    }

    public static boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        if (!(screen instanceof ChatScreen)) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();

        int startX = ChannelHudLayoutConfig.chatX();
        int x = startX;
        int y = ChannelHudLayoutConfig.chatY() - 24;

        for (ClientChannelTabState.TabEntry tab : visibleTabsForCurrentPage()) {
            int width = ChannelTabLayout.computeWidth(tab.displayName());

            if (mouseX >= x
                    && mouseX <= x + width
                    && mouseY >= y
                    && mouseY <= y + ChannelTabLayout.TAB_HEIGHT) {

                ClientChannelChatState.resetScroll(tab.id());
                ClientChannelNotifications.clearUnread(tab.id());
                ClientPlayNetworking.send(new ChannelSelectTabPayload(tab.id()));
                return true;
            }

            x += width + 2;
        }

        int pageY = y - 12;
        int charWidth = mc.font.width(">");

        int leftArrowX1 = startX;
        int leftArrowX2 = startX + charWidth + 4;

        if (mouseX >= leftArrowX1
                && mouseX <= leftArrowX2
                && mouseY >= pageY
                && mouseY <= pageY + 10) {

            ClientPlayNetworking.send(new ChannelChangePagePayload(false));
            return true;
        }

        String pageText = pageIndicatorText(ClientChannelTabState.getTabs());
        int pageTextWidth = mc.font.width(pageText);

        int rightArrowX2 = startX + pageTextWidth;
        int rightArrowX1 = rightArrowX2 - charWidth - 4;

        if (mouseX >= rightArrowX1
                && mouseX <= rightArrowX2
                && mouseY >= pageY
                && mouseY <= pageY + 10) {

            ClientPlayNetworking.send(new ChannelChangePagePayload(true));
            return true;
        }

        return false;
    }
}