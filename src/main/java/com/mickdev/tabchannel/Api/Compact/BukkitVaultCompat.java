package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public final class BukkitVaultCompat implements PermissionCompat {

    private Object permissionProvider() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object servicesManager = bukkitClass.getMethod("getServicesManager").invoke(null);

            Class<?> permissionClass = Class.forName("net.milkbowl.vault.permission.Permission");
            Object registration = servicesManager.getClass()
                    .getMethod("getRegistration", Class.class)
                    .invoke(servicesManager, permissionClass);

            if (registration == null) {
                return null;
            }

            return registration.getClass().getMethod("getProvider").invoke(registration);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) {
            return false;
        }

        if (player.hasPermissions(2)) {
            return true;
        }

        try {
            Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
            Object provider = permissionProvider();

            if (bukkitPlayer == null || provider == null) {
                return false;
            }

            Object result = provider.getClass()
                    .getMethod("playerHas", String.class, Class.forName("org.bukkit.OfflinePlayer"), String.class)
                    .invoke(provider, null, bukkitPlayer, node);

            return result instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public String getPrefix(ServerPlayer player) {
        try {
            Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
            Object provider = permissionProvider();

            if (bukkitPlayer == null || provider == null) {
                return "";
            }

            Object result = provider.getClass()
                    .getMethod("getPlayerPrefix", String.class, Class.forName("org.bukkit.OfflinePlayer"))
                    .invoke(provider, null, bukkitPlayer);

            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public String getSuffix(ServerPlayer player) {
        try {
            Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
            Object provider = permissionProvider();

            if (bukkitPlayer == null || provider == null) {
                return "";
            }

            Object result = provider.getClass()
                    .getMethod("getPlayerSuffix", String.class, Class.forName("org.bukkit.OfflinePlayer"))
                    .invoke(provider, null, bukkitPlayer);

            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public String getPrimaryGroup(ServerPlayer player) {
        try {
            Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
            Object provider = permissionProvider();

            if (bukkitPlayer == null || provider == null) {
                return "";
            }

            Object result = provider.getClass()
                    .getMethod("getPrimaryGroup", String.class, Class.forName("org.bukkit.OfflinePlayer"))
                    .invoke(provider, null, bukkitPlayer);

            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
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
