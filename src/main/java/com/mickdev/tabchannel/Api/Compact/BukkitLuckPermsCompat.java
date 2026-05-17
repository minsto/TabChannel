package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public final class BukkitLuckPermsCompat implements PermissionCompat {

    @Override
    public boolean hasPermission(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) {
            return false;
        }

        if (player.hasPermissions(2)) {
            return true;
        }

        Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
        if (bukkitPlayer == null) {
            return false;
        }

        try {
            Object result = bukkitPlayer.getClass()
                    .getMethod("hasPermission", String.class)
                    .invoke(bukkitPlayer, node);

            return result instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public String getPrefix(ServerPlayer player) {
        return getLuckPermsMeta(player, "getPrefix");
    }

    @Override
    public String getSuffix(ServerPlayer player) {
        return getLuckPermsMeta(player, "getSuffix");
    }

    @Override
    public String getPrimaryGroup(ServerPlayer player) {
        try {
            Object user = getLuckPermsUser(player);
            if (user == null) {
                return "";
            }

            Object result = user.getClass().getMethod("getPrimaryGroup").invoke(user);
            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public String getMeta(ServerPlayer player, String key) {
        if (key == null || key.isBlank()) {
            return "";
        }

        try {
            Object metaData = getCachedMetaData(player);
            if (metaData == null) {
                return "";
            }

            Object result = metaData.getClass()
                    .getMethod("getMetaValue", String.class)
                    .invoke(metaData, key);

            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public ChatFormatting getNameColor(ServerPlayer player) {
        String value = getMeta(player, "namecolor");

        if (value == null || value.isBlank()) {
            value = getMeta(player, "name_color");
        }

        ChatFormatting formatting = ChatFormatting.getByName(value == null ? "" : value.toLowerCase());
        return formatting == null ? ChatFormatting.WHITE : formatting;
    }

    private String getLuckPermsMeta(ServerPlayer player, String methodName) {
        try {
            Object metaData = getCachedMetaData(player);
            if (metaData == null) {
                return "";
            }

            Object result = metaData.getClass().getMethod(methodName).invoke(metaData);
            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private Object getCachedMetaData(ServerPlayer player) {
        try {
            Object user = getLuckPermsUser(player);
            if (user == null) {
                return null;
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            if (cachedData == null) {
                return null;
            }

            return cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object getLuckPermsUser(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = providerClass.getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);

            return userManager.getClass()
                    .getMethod("getUser", java.util.UUID.class)
                    .invoke(userManager, player.getUUID());
        } catch (Throwable ignored) {
            return null;
        }
    }
}