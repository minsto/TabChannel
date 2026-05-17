package com.mickdev.tabchannel.Api.Compact;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public final class LuckPermsCompat implements PermissionCompat {

    private LuckPerms api() {
        try {
            return LuckPermsProvider.get();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private User user(ServerPlayer player) {
        LuckPerms api = api();
        if (api == null || player == null) {
            return null;
        }

        return api.getUserManager().getUser(player.getUUID());
    }

    private CachedMetaData meta(ServerPlayer player) {
        User user = user(player);
        return user == null ? null : user.getCachedData().getMetaData();
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) {
            return false;
        }

        if (player.hasPermissions(2)) {
            return true;
        }

        User user = user(player);
        return user != null && user.getCachedData().getPermissionData().checkPermission(node).asBoolean();
    }

    @Override
    public String getPrefix(ServerPlayer player) {
        CachedMetaData meta = meta(player);
        return meta == null || meta.getPrefix() == null ? "" : meta.getPrefix();
    }

    @Override
    public String getSuffix(ServerPlayer player) {
        CachedMetaData meta = meta(player);
        return meta == null || meta.getSuffix() == null ? "" : meta.getSuffix();
    }

    @Override
    public String getPrimaryGroup(ServerPlayer player) {
        User user = user(player);
        return user == null ? "" : user.getPrimaryGroup();
    }

    @Override
    public String getMeta(ServerPlayer player, String key) {
        CachedMetaData meta = meta(player);
        if (meta == null || key == null || key.isBlank()) {
            return "";
        }

        String value = meta.getMetaValue(key);
        return value == null ? "" : value;
    }

    @Override
    public ChatFormatting getNameColor(ServerPlayer player) {
        String value = getMeta(player, "namecolor");

        if (value == null || value.isBlank()) {
            value = getMeta(player, "name_color");
        }

        if (value == null || value.isBlank()) {
            return ChatFormatting.WHITE;
        }

        ChatFormatting formatting = ChatFormatting.getByName(value.toLowerCase());
        return formatting == null ? ChatFormatting.WHITE : formatting;
    }
}
