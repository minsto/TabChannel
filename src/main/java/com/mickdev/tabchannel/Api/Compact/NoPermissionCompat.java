package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public final class NoPermissionCompat implements PermissionCompat {

    @Override
    public boolean hasPermission(ServerPlayer player, String node) {
        return player != null && player.hasPermissions(2);
    }

    @Override
    public String getPrefix(ServerPlayer player) {
        return "";
    }

    @Override
    public String getSuffix(ServerPlayer player) {
        return "";
    }

    @Override
    public String getPrimaryGroup(ServerPlayer player) {
        return "";
    }

    @Override
    public String getMeta(ServerPlayer player, String key) {
        return "";
    }

    @Override
    public ChatFormatting getNameColor(ServerPlayer player) {
        return ChatFormatting.WHITE;
    }
}
