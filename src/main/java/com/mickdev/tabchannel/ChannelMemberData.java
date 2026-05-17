package com.mickdev.tabchannel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ChannelMemberData {

    private final UUID playerId;
    private ChannelRole role;
    private final Set<String> permissions = new HashSet<>();
    private long mutedUntil;
    private int warnCount;

    public ChannelMemberData(UUID playerId, ChannelRole role) {
        this.playerId = playerId;
        this.role = role;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public ChannelRole getRole() {
        return role;
    }

    public void setRole(ChannelRole role) {
        this.role = role;
    }

    public boolean hasPermission(String perm) {
        return role == ChannelRole.OWNER || permissions.contains("*") || permissions.contains(perm);
    }

    public void grant(String perm) {
        permissions.add(perm);
    }

    public void revoke(String perm) {
        permissions.remove(perm);
    }

    public long getMutedUntil() {
        return mutedUntil;
    }

    public void setMutedUntil(long mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public boolean isMutedNow() {
        return System.currentTimeMillis() < mutedUntil;
    }

    public int getWarnCount() {
        return warnCount;
    }

    public void setWarnCount(int warnCount) {
        this.warnCount = warnCount;
    }

    public int addWarn() {
        this.warnCount++;
        return this.warnCount;
    }

    public void resetWarns() {
        this.warnCount = 0;
    }
}