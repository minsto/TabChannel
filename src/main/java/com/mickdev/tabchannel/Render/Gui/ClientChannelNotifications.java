package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;
import java.util.Map;

public final class ClientChannelNotifications {

    private static final Map<String, Integer> UNREAD = new HashMap<>();

    private static Component lastPing = Component.empty();
    private static long lastPingTimeMs = 0L;
    private static String lastPingChannel = "";

    private ClientChannelNotifications() {
    }

    public static void onMessage(String channelId, Component message) {
        if (channelId == null || channelId.isBlank() || message == null) {
            return;
        }

        String selected = ClientChannelTabState.getSelectedChannelId();

        if (!channelId.equalsIgnoreCase(selected)) {
            UNREAD.put(channelId, getUnread(channelId) + 1);
        }
    }

    /** Ping HUD + son (serveur ou canal global via {@link com.mickdev.tabchannel.mention.ChannelMentionService}). */
    public static void onMentionNotify(String channelId, Component preview) {
        if (channelId == null || channelId.isBlank() || preview == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        lastPing = preview;
        lastPingChannel = channelId;
        lastPingTimeMs = System.currentTimeMillis();
        mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.55F, 1.25F);
    }

    public static int getUnread(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return 0;
        }

        return Math.max(0, UNREAD.getOrDefault(channelId, 0));
    }

    public static void clearUnread(String channelId) {
        if (channelId != null && !channelId.isBlank()) {
            UNREAD.remove(channelId);
        }
    }

    public static Component getLastPing() {
        return lastPing;
    }

    public static String getLastPingChannel() {
        return lastPingChannel;
    }

    public static boolean hasFreshPing() {
        return System.currentTimeMillis() - lastPingTimeMs < 5500L;
    }
}
