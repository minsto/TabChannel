package com.mickdev.tabchannel.Api.Compact;
public final class BukkitDetector {

    private BukkitDetector() {
    }

    public static boolean isBukkitPresent() {
        try {
            Class.forName("org.bukkit.Bukkit");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean hasPlugin(String pluginName) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
            Object plugin = pluginManager.getClass().getMethod("getPlugin", String.class).invoke(pluginManager, pluginName);

            if (plugin == null) {
                return false;
            }

            return (boolean) plugin.getClass().getMethod("isEnabled").invoke(plugin);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static Object getBukkitPlayer(java.util.UUID uuid) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            return bukkitClass.getMethod("getPlayer", java.util.UUID.class).invoke(null, uuid);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
