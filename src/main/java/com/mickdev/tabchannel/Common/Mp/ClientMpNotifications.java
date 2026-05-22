package com.mickdev.tabchannel.Common.Mp;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ClientMpNotifications {

    private static final Map<String, Integer> UNREAD = new HashMap<>();
    private static String activePeer = "";
    private static boolean screenOpen;

    private ClientMpNotifications() {
    }

    public static void onIncoming(String peerName) {
        if (peerName == null || peerName.isBlank()) {
            return;
        }

        String key = ClientMpChatStore.normalize(peerName);

        if (screenOpen && key.equals(activePeer)) {
            return;
        }

        UNREAD.merge(key, 1, Integer::sum);
        ClientMpPersistence.saveForCurrentServer();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.55F, 1.35F);
        }
    }

    public static int getUnread(String peerName) {
        if (peerName == null || peerName.isBlank()) {
            return 0;
        }

        return Math.max(0, UNREAD.getOrDefault(ClientMpChatStore.normalize(peerName), 0));
    }

    public static int getTotalUnread() {
        int total = 0;

        for (int count : UNREAD.values()) {
            total += count;
        }

        return total;
    }

    public static boolean hasHudAlert() {
        return getTotalUnread() > 0;
    }

    public static void clearUnread(String peerName) {
        if (peerName != null && !peerName.isBlank()) {
            UNREAD.remove(ClientMpChatStore.normalize(peerName));
            ClientMpPersistence.saveForCurrentServer();
        }
    }

    public static void setActivePeer(String peerName) {
        activePeer = peerName == null || peerName.isBlank()
                ? ""
                : ClientMpChatStore.normalize(peerName);
    }

    public static void setScreenOpen(boolean open) {
        screenOpen = open;

        if (!open) {
            activePeer = "";
        }
    }

    public static void loadUnread(Map<String, Integer> unread) {
        UNREAD.clear();

        if (unread != null) {
            for (Map.Entry<String, Integer> entry : unread.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                    UNREAD.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
                }
            }
        }
    }

    public static Map<String, Integer> exportUnread() {
        return Map.copyOf(UNREAD);
    }

    public static void clearMemory() {
        UNREAD.clear();
        activePeer = "";
        screenOpen = false;
    }

    public static void resetSession() {
        clearMemory();
    }
}
