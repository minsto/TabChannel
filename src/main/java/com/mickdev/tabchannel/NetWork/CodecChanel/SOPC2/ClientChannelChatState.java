package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import net.minecraft.network.chat.Component;
import com.mickdev.tabchannel.Render.Gui.ClientChannelNotifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientChannelChatState {

    public record TimedMessage(Component message, long timeMs) {}

    private static final Map<String, List<TimedMessage>> HISTORY = new HashMap<>();
    private static final Map<String, Integer> SCROLL_OFFSETS = new HashMap<>();
    private static final int MAX_MESSAGES_PER_CHANNEL = 200;

    private ClientChannelChatState() {
    }
    public static void clearChannel(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }

        HISTORY.remove(channelId);
        SCROLL_OFFSETS.remove(channelId);
    }

    public static void push(String channelId, Component message) {
        if (channelId == null || channelId.isBlank() || message == null) {
            return;
        }

        List<TimedMessage> list = HISTORY.computeIfAbsent(channelId, k -> new ArrayList<>());
        list.add(new TimedMessage(message, System.currentTimeMillis()));
        ClientChannelNotifications.onMessage(channelId, message);

        while (list.size() > MAX_MESSAGES_PER_CHANNEL) {
            list.remove(0);
        }

        // Si le joueur est déjà en bas, on reste collé en bas.
        int offset = SCROLL_OFFSETS.getOrDefault(channelId, 0);
        if (offset < 0) {
            SCROLL_OFFSETS.put(channelId, 0);
        }
    }

    public static List<TimedMessage> getTimedMessages(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return List.of();
        }
        return HISTORY.getOrDefault(channelId, List.of());
    }

    public static List<Component> getMessages(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return List.of();
        }

        List<TimedMessage> timed = HISTORY.getOrDefault(channelId, List.of());
        List<Component> result = new ArrayList<>();

        for (TimedMessage entry : timed) {
            result.add(entry.message());
        }

        return result;
    }

    public static int getScrollOffset(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return 0;
        }
        return Math.max(0, SCROLL_OFFSETS.getOrDefault(channelId, 0));
    }

    public static void setScrollOffset(String channelId, int offset) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }
        SCROLL_OFFSETS.put(channelId, Math.max(0, offset));
    }

    public static void scrollUp(String channelId, int amount, int visibleLines) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }

        int current = getScrollOffset(channelId);
        int max = getMaxScrollOffset(channelId, visibleLines);
        setScrollOffset(channelId, Math.min(max, current + Math.max(1, amount)));
    }

    public static void scrollDown(String channelId, int amount) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }

        int current = getScrollOffset(channelId);
        setScrollOffset(channelId, Math.max(0, current - Math.max(1, amount)));
    }

    public static int getMaxScrollOffset(String channelId, int visibleLines) {
        List<Component> messages = getMessages(channelId);
        if (messages.isEmpty()) {
            return 0;
        }

        int max = messages.size() - Math.max(1, visibleLines);
        return Math.max(0, max);
    }

    public static void resetScroll(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }
        SCROLL_OFFSETS.put(channelId, 0);
    }

    public static void clearAll() {
        HISTORY.clear();
        SCROLL_OFFSETS.clear();
    }
}