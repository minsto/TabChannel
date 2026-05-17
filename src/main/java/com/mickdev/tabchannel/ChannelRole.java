package com.mickdev.tabchannel;


public enum ChannelRole {
    OWNER,
    ADMIN,
    MODERATOR,
    MEMBER;

    public boolean canManage() {
        return this == OWNER || this == ADMIN || this == MODERATOR;
    }

    public boolean canDelete() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canGrant() {
        return this == OWNER || this == ADMIN;
    }
}