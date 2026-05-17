package com.mickdev.tabchannel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatTabState {

    private final UUID playerId;
    private final List<String> openedTabs = new ArrayList<>();
    private String selectedChannelId = "global";
    private int page = 0;

    public ChatTabState(UUID playerId) {
        this.playerId = playerId;
        this.openedTabs.add("global");
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public List<String> getOpenedTabs() {
        return openedTabs;
    }

    public String getSelectedChannelId() {
        return selectedChannelId;
    }

    public void setSelectedChannelId(String selectedChannelId) {
        this.selectedChannelId = selectedChannelId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public void openTab(String channelId) {
        if (!openedTabs.contains(channelId)) {
            openedTabs.add(channelId);
        }
    }

    public void closeTab(String channelId) {
        if (!"global".equals(channelId)) {
            openedTabs.remove(channelId);
            if (channelId.equals(selectedChannelId)) {
                selectedChannelId = "global";
            }
        }
    }
}