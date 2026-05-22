package com.mickdev.tabchannel;





import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.text.SimpleDateFormat;
import java.util.*;

public final class ChatManager {

    public static final int MAX_VISIBLE_TABS = 5;

    public static final Map<String, ChatChannel> CHANNELS = new LinkedHashMap<>();
    public static final Map<String, List<ChatEntry>> TABS = new LinkedHashMap<>();
    private static final Map<UUID, ChatTabState> PLAYER_STATES = new HashMap<>();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    static {
        ChatChannel global = new ChatChannel("global", "Global", ChannelVisibility.PUBLIC, new UUID(0L, 0L));
        global.setOriginalGlobal(true);
        CHANNELS.put(global.getId(), global);
        TABS.put(global.getId(), new ArrayList<>());
    }

    private ChatManager() {
    }

    public static ChatChannel createChannel(String name, ChannelVisibility visibility, ServerPlayer owner) {
        String cleanId = sanitizeId(name);

        if (cleanId.isBlank()) {
            throw new IllegalArgumentException("Nom de canal invalide");
        }
        if (CHANNELS.containsKey(cleanId)) {
            throw new IllegalArgumentException("Ce canal existe déjà");
        }

        ChatChannel channel = new ChatChannel(cleanId, name, visibility, owner.getUUID());
        channel.addMember(owner.getUUID(), ChannelRole.OWNER);

        CHANNELS.put(cleanId, channel);
        TABS.put(cleanId, new ArrayList<>());

        ChatTabState state = getState(owner.getUUID());
        state.openTab(cleanId);
        state.setSelectedChannelId(cleanId);

        ensureSelectedTabVisible(owner.getUUID());

        return channel;
    }

    public static ChatChannel getChannel(String channelId) {
        return CHANNELS.get(sanitizeId(channelId));
    }

    public static Collection<ChatChannel> getChannels() {
        return CHANNELS.values();
    }

    public static Iterable<ChatChannel> getAllChannels() {
        return new ArrayList<>(CHANNELS.values());
    }

    public static void registerChannel(ChatChannel channel) {
        if (channel == null) {
            return;
        }

        CHANNELS.put(channel.getId(), channel);
        TABS.putIfAbsent(channel.getId(), new ArrayList<>());
    }

    public static ChatTabState getState(UUID playerId) {
        return PLAYER_STATES.computeIfAbsent(playerId, ChatTabState::new);
    }

    public static void joinChannel(ChatChannel channel, ServerPlayer player, boolean bypass) {
        if (channel == null) {
            throw new IllegalStateException(
                    Component.translatable("tabchannel.error.channel_not_found")
                            .getString()
            );
        }

        if (channel.isBanned(player.getUUID()) && !bypass) {
            throw new IllegalStateException(
                    Component.translatable("tabchannel.error.you_are_banned")
                            .getString()
            );
        }

        if ((channel.getVisibility() == ChannelVisibility.PRIVATE
                || channel.getVisibility() == ChannelVisibility.FACTION)
                && !channel.isMember(player.getUUID())
                && !bypass) {

            throw new IllegalStateException(
                    Component.translatable("tabchannel.error.private_channel_access")
                            .getString()
            );
        }

        if (!channel.isMember(player.getUUID())) {
            channel.addMember(player.getUUID(), ChannelRole.MEMBER);
        }

        ChatTabState state = getState(player.getUUID());
        state.openTab(channel.getId());
        state.setSelectedChannelId(channel.getId());

        ensureSelectedTabVisible(player.getUUID());
    }

    public static void joinAndSelect(UUID playerId, String channelId) {
        ChatTabState state = getState(playerId);
        ChatChannel channel = getChannel(channelId);

        if (channel == null) {
            return;
        }

        if (!state.getOpenedTabs().contains(channel.getId())) {
            state.openTab(channel.getId());
        }

        state.setSelectedChannelId(channel.getId());
        ensureSelectedTabVisible(playerId);
    }

    public static void selectTab(UUID playerId, String channelId) {
        ChatTabState state = getState(playerId);
        ChatChannel channel = getChannel(channelId);

        if (channel == null) {
            return;
        }

        if (!state.getOpenedTabs().contains(channel.getId())) {
            return;
        }

        state.setSelectedChannelId(channel.getId());
        ensureSelectedTabVisible(playerId);
    }

    public static boolean invitePlayer(String channelId, ServerPlayer target) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null) {
            return false;
        }

        if (!channel.isMember(target.getUUID())) {
            channel.addMember(target.getUUID(), ChannelRole.MEMBER);
        }

        ChatTabState state = getState(target.getUUID());
        state.openTab(channel.getId());

        ChannelSyncService.syncPlayer(target);
        return true;
    }

    public static void leaveChannel(UUID playerId, String channelId) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null || channel.isOriginalGlobal()) {
            return;
        }

        channel.removeMember(playerId);
        getState(playerId).closeTab(channelId);

        rebuildPlayerTabs(playerId);
    }

    public static void deleteChannel(String channelId) {
        String cleanId = sanitizeId(channelId);
        ChatChannel channel = CHANNELS.get(cleanId);

        if (channel == null || channel.isOriginalGlobal()) {
            return;
        }

        CHANNELS.remove(cleanId);
        TABS.remove(cleanId);

        for (Map.Entry<UUID, ChatTabState> entry : PLAYER_STATES.entrySet()) {
            UUID playerId = entry.getKey();
            ChatTabState state = entry.getValue();

            state.closeTab(cleanId);

            if (cleanId.equals(state.getSelectedChannelId())) {
                state.setSelectedChannelId("global");
            }

            ensurePageInBounds(playerId);
        }
    }

    public static void push(String channelId, Component message) {
        String cleanId = sanitizeId(channelId);
        String plain = message.getString();
        String stamp = "[" + FORMAT.format(new Date()) + "] ";
        Component finalMsg = Component.literal(stamp).append(message);

        List<ChatEntry> list = TABS.computeIfAbsent(cleanId, s -> new ArrayList<>());
        String fp = cleanId + "|" + plain;

        if (!list.isEmpty()) {
            ChatEntry last = list.get(list.size() - 1);
            if (last.fingerprint().equals(fp)) {
                return;
            }
        }

        list.add(new ChatEntry(cleanId, finalMsg, System.currentTimeMillis(), fp));
    }

    public static List<ChatEntry> get(String channelId) {
        return TABS.computeIfAbsent(sanitizeId(channelId), s -> new ArrayList<>());
    }

    public static List<String> getVisibleTabs(UUID playerId) {
        ChatTabState state = getState(playerId);
        List<String> all = state.getOpenedTabs();

        ensurePageInBounds(playerId);

        int start = state.getPage() * MAX_VISIBLE_TABS;
        int end = Math.min(start + MAX_VISIBLE_TABS, all.size());

        if (start < 0 || start >= all.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(all.subList(start, end));
    }

    public static int getPageCount(UUID playerId) {
        int size = getState(playerId).getOpenedTabs().size();
        return Math.max(1, (size + MAX_VISIBLE_TABS - 1) / MAX_VISIBLE_TABS);
    }

    public static boolean hasPrevPage(UUID playerId) {
        return getState(playerId).getPage() > 0;
    }

    public static boolean hasNextPage(UUID playerId) {
        ChatTabState state = getState(playerId);
        return state.getPage() + 1 < getPageCount(playerId);
    }

    public static void nextPage(UUID playerId) {
        ChatTabState state = getState(playerId);
        if (hasNextPage(playerId)) {
            state.setPage(state.getPage() + 1);
        }
    }

    public static void prevPage(UUID playerId) {
        ChatTabState state = getState(playerId);
        if (hasPrevPage(playerId)) {
            state.setPage(state.getPage() - 1);
        }
    }

    public static void clearAllDynamicChannels() {
        CHANNELS.entrySet().removeIf(e -> !e.getValue().isOriginalGlobal());
        TABS.entrySet().removeIf(e -> !"global".equals(e.getKey()));

        for (UUID playerId : PLAYER_STATES.keySet()) {
            rebuildPlayerTabs(playerId);
        }
    }

    /**
     * Nouveau monde / redémarrage du serveur intégré : les maps statiques survivent en JVM,
     * il faut tout remettre à zéro avant de charger le SavedData du monde courant.
     */
    public static void resetForNewServer() {
        CHANNELS.entrySet().removeIf(e -> !e.getValue().isOriginalGlobal());
        TABS.entrySet().removeIf(e -> !"global".equals(e.getKey()));
        TABS.computeIfAbsent("global", id -> new ArrayList<>()).clear();
        PLAYER_STATES.clear();
    }

    public static void addLoadedChannel(ChatChannel channel) {
        CHANNELS.put(channel.getId(), channel);
        TABS.putIfAbsent(channel.getId(), new ArrayList<>());
    }

    public static String sanitizeId(String raw) {
        return raw.toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replaceAll("[^a-z0-9_\\-]", "");
    }

    public static void rebuildPlayerTabs(UUID playerId) {
        ChatTabState state = getState(playerId);

        String oldSelected = state.getSelectedChannelId();
        int oldPage = state.getPage();

        state.getOpenedTabs().clear();
        state.openTab("global");

        for (ChatChannel channel : CHANNELS.values()) {
            if (channel.isOriginalGlobal()) {
                continue;
            }

            if (channel.isMember(playerId)) {
                state.openTab(channel.getId());
            }
        }

        if (oldSelected != null) {
            if ("global".equals(oldSelected)) {
                state.setSelectedChannelId("global");
            } else {
                ChatChannel selectedChannel = getChannel(oldSelected);
                if (selectedChannel != null && selectedChannel.isMember(playerId)) {
                    state.setSelectedChannelId(oldSelected);
                } else {
                    state.setSelectedChannelId("global");
                }
            }
        } else {
            state.setSelectedChannelId("global");
        }

        // IMPORTANT :
        // on garde la page actuelle si possible
        state.setPage(oldPage);
        ensurePageInBounds(playerId);
    }

    public static void ensureSelectedTabVisible(UUID playerId) {
        ChatTabState state = getState(playerId);
        String selected = state.getSelectedChannelId();

        if (selected == null || selected.isBlank()) {
            state.setSelectedChannelId("global");
            selected = "global";
        }

        List<String> tabs = state.getOpenedTabs();
        int index = tabs.indexOf(selected);

        if (index < 0) {
            state.setSelectedChannelId("global");
            index = tabs.indexOf("global");
        }

        if (index < 0) {
            state.setPage(0);
            return;
        }

        int page = index / MAX_VISIBLE_TABS;
        state.setPage(page);
    }

    public static void ensurePageInBounds(UUID playerId) {
        ChatTabState state = getState(playerId);
        int pageCount = getPageCount(playerId);

        if (state.getPage() < 0) {
            state.setPage(0);
            return;
        }

        if (state.getPage() >= pageCount) {
            state.setPage(Math.max(0, pageCount - 1));
        }
    }
}