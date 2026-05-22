package com.mickdev.tabchannel.Common.Mp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientMpChatStore {

    public record MpLine(String text, boolean incoming, long timeMs) {
    }

    public record SearchHit(String peerKey, String peerDisplayName, MpLine line) {
    }

    private static final int MAX_LINES_PER_PEER = 500;
    private static final Map<String, List<MpLine>> CONVERSATIONS = new LinkedHashMap<>();
    private static final Map<String, String> DISPLAY_NAMES = new LinkedHashMap<>();

    private ClientMpChatStore() {
    }

    public static void addMessage(String peerName, String text, boolean incoming) {
        addMessage(peerName, text, incoming, System.currentTimeMillis(), true);
    }

    public static void addMessageRaw(
            String peerName,
            String text,
            boolean incoming,
            long timeMs,
            boolean persist
    ) {
        if (peerName == null || peerName.isBlank() || text == null || text.isBlank()) {
            return;
        }

        String key = normalize(peerName);
        DISPLAY_NAMES.put(key, peerName.trim());

        List<MpLine> lines = CONVERSATIONS.computeIfAbsent(key, ignored -> new ArrayList<>());
        lines.add(new MpLine(text.trim(), incoming, timeMs));

        while (lines.size() > MAX_LINES_PER_PEER) {
            lines.removeFirst();
        }

        if (persist) {
            ClientMpPersistence.saveForCurrentServer();
        }
    }

    private static void addMessage(String peerName, String text, boolean incoming, long timeMs, boolean persist) {
        addMessageRaw(peerName, text, incoming, timeMs, persist);
    }

    public static List<MpLine> getMessages(String peerName) {
        if (peerName == null || peerName.isBlank()) {
            return List.of();
        }

        return List.copyOf(CONVERSATIONS.getOrDefault(normalize(peerName), List.of()));
    }

    public static List<String> getAllPeerKeys() {
        return List.copyOf(CONVERSATIONS.keySet());
    }

    public static String getDisplayName(String peerKeyOrName) {
        if (peerKeyOrName == null || peerKeyOrName.isBlank()) {
            return "";
        }

        String key = normalize(peerKeyOrName);
        return DISPLAY_NAMES.getOrDefault(key, peerKeyOrName);
    }

    public static void loadDisplayNames(Map<String, String> names) {
        DISPLAY_NAMES.clear();

        if (names != null) {
            DISPLAY_NAMES.putAll(names);
        }
    }

    public static Map<String, String> exportDisplayNames() {
        return Map.copyOf(DISPLAY_NAMES);
    }

    public static List<SearchHit> searchMessages(String query, int limit) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();

        if (q.isBlank()) {
            return List.of();
        }

        List<SearchHit> hits = new ArrayList<>();

        for (Map.Entry<String, List<MpLine>> entry : CONVERSATIONS.entrySet()) {
            String display = getDisplayName(entry.getKey());

            for (MpLine line : entry.getValue()) {
                if (line.text().toLowerCase(Locale.ROOT).contains(q)
                        || display.toLowerCase(Locale.ROOT).contains(q)) {
                    hits.add(new SearchHit(entry.getKey(), display, line));
                }
            }
        }

        hits.sort(Comparator.comparingLong((SearchHit hit) -> hit.line().timeMs()).reversed());
        return hits.stream().limit(limit).toList();
    }

    public static void clearMemory() {
        CONVERSATIONS.clear();
        DISPLAY_NAMES.clear();
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
