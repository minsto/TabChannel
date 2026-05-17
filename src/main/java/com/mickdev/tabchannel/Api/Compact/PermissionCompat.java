package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public interface PermissionCompat {

    boolean hasPermission(ServerPlayer player, String node);

    String getPrefix(ServerPlayer player);

    String getSuffix(ServerPlayer player);

    String getPrimaryGroup(ServerPlayer player);

    String getMeta(ServerPlayer player, String key);

    ChatFormatting getNameColor(ServerPlayer player);
}