package com.mickdev.tabchannel;


import java.util.*;

public final class ChatChannel {
    private boolean factionChannel;
    private String factionId;
    private final String id;
    private String displayName;
    private ChannelVisibility visibility;
    private UUID owner;
    private boolean originalGlobal;
    private boolean antiSwear;
    private String rules = "";
    private String tabColor = "WHITE";
    private boolean staffChannel = false;
    private final Map<UUID, ChannelMemberData> members = new LinkedHashMap<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    public ChatChannel(String id, String displayName, ChannelVisibility visibility, UUID owner) {
        this.id = id;
        this.displayName = displayName;
        this.visibility = visibility;
        this.owner = owner;
    }
    public boolean isFactionChannel() {
        return factionChannel;
    }
    public String getTabColor() {
        return tabColor == null || tabColor.isBlank() ? "WHITE" : tabColor;
    }

    public void setTabColor(String tabColor) {
        this.tabColor = tabColor;
    }

    public boolean isStaffChannel() {
        return staffChannel;
    }

    public void setStaffChannel(boolean staffChannel) {
        this.staffChannel = staffChannel;
    }
    public void setFactionChannel(boolean factionChannel) {
        this.factionChannel = factionChannel;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ChannelVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ChannelVisibility visibility) {
        this.visibility = visibility;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isOriginalGlobal() {
        return originalGlobal;
    }

    public void setOriginalGlobal(boolean originalGlobal) {
        this.originalGlobal = originalGlobal;
    }

    public boolean isAntiSwear() {
        return antiSwear;
    }

    public void setAntiSwear(boolean antiSwear) {
        this.antiSwear = antiSwear;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules == null ? "" : rules;
    }

    public Map<UUID, ChannelMemberData> getMembers() {
        return members;
    }

    public ChannelMemberData getMember(UUID playerId) {
        return members.get(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public void addMember(UUID playerId, ChannelRole role) {
        members.put(playerId, new ChannelMemberData(playerId, role));
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public boolean isBanned(UUID playerId) {
        return bannedPlayers.contains(playerId);
    }

    public void ban(UUID playerId) {
        bannedPlayers.add(playerId);
        members.remove(playerId);
    }

    public void unban(UUID playerId) {
        bannedPlayers.remove(playerId);
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }
}