package com.mickdev.tabchannel.Render.Gui;


import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ClientChannelChatState;
import com.mickdev.tabchannel.Render.Hud.MpButtonRenderer;
import com.mickdev.tabchannel.Render.Hud.MpHudInteraction;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class ChannelChatScreenHook {

    private ChannelChatScreenHook() {
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ChatScreen) {
            ChannelTabRenderer.render(
                    event.getScreen(),
                    event.getGuiGraphics(),
                    event.getMouseX(),
                    event.getMouseY()
            );

            ChannelMessageRenderer.render(
                    event.getScreen(),
                    event.getGuiGraphics(),
                    event.getMouseX(),
                    event.getMouseY()
            );

            Minecraft mc = Minecraft.getInstance();
            MpButtonRenderer.renderOnChat(event.getGuiGraphics(), mc, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (MpHudInteraction.handlePopoutClick(event.getMouseX(), event.getMouseY(), event.getButton(), mc)) {
            event.setCanceled(true);
            return;
        }

        if (ChannelMessageRenderer.mouseClicked(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton()
        )) {
            event.setCanceled(true);
            return;
        }

        if (ChannelTabRenderer.mouseClicked(
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton()
        )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void mouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }

        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();
        if (selectedChannelId == null || selectedChannelId.isBlank() || "global".equals(selectedChannelId)) {
            return;
        }

        int visibleLines = ChannelMessageRenderer.getVisibleLineCount(event.getScreen());

        if (event.getScrollDeltaY() > 0) {
            ClientChannelChatState.scrollUp(selectedChannelId, 1, visibleLines);
            event.setCanceled(true);
        } else if (event.getScrollDeltaY() < 0) {
            ClientChannelChatState.scrollDown(selectedChannelId, 1);
            event.setCanceled(true);
        }
    }
}