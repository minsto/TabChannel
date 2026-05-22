package com.mickdev.tabchannel.Init;

import com.mickdev.tabchannel.Api.Compact.BukkitDetector;
import com.mickdev.tabchannel.TabChannel;

/**
 * Youer / Paper / Bukkit sans LuckPerms : déclare les permissions des commandes
 * avec {@code PermissionDefault.TRUE} pour que tous les joueurs puissent les utiliser.
 */
public final class HybridBukkitPermissions {

    private static final String[] PERMISSION_NODES = {
            "tabchannel.command",
            "tabchannel.command.channel",
            "tabchannel.command.ch",
            "tabchannel.command.tc",
            "tabchannel.command.tb",
            "tabchannel.command.tabchannel",
            "tabchannel.command.channelcreate",
            "tabchannel.command.channeljoin",
            "tabchannel.command.channellist",
            "tabchannel.command.setchannel",
            "setchannel",
            "setchannel.create",
            "setchannel.delete",
            "setchannel.invite",
            "setchannel.leave",
            "setchannel.perm",
            "setchannel.rules",
            "setchannel.warn",
            "setchannel.kick",
            "setchannel.ban",
            "setchannel.tabcolors",
            "setchannel.f",
            "setchannel.bypass.join",
            "setchannel.bypass.private",
            "setchannel.bypass.ban"
    };

    private HybridBukkitPermissions() {}

    public static void registerDefaultsForAllPlayers() {
        if (!BukkitDetector.isBukkitPresent()) {
            return;
        }

        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
            Class<?> permClass = Class.forName("org.bukkit.permissions.Permission");
            Class<?> defaultClass = Class.forName("org.bukkit.permissions.PermissionDefault");

            @SuppressWarnings({"unchecked", "rawtypes"})
            Object defaultTrue = Enum.valueOf((Class) defaultClass, "TRUE");

            for (String node : PERMISSION_NODES) {
                registerNode(pluginManager, permClass, defaultClass, defaultTrue, node);
            }

            TabChannel.LOGGER.info("[TabChannel] Permissions Bukkit par défaut (tous joueurs) enregistrées pour Youer/Paper.");
        } catch (Throwable t) {
            TabChannel.LOGGER.warn("[TabChannel] Impossible d'enregistrer les permissions Bukkit par défaut : {}", t.getMessage());
        }
    }

    private static void registerNode(
            Object pluginManager,
            Class<?> permClass,
            Class<?> defaultClass,
            Object defaultTrue,
            String node
    ) {
        try {
            Object existing = pluginManager.getClass().getMethod("getPermission", String.class).invoke(pluginManager, node);
            if (existing != null) {
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            Object permission = permClass
                    .getConstructor(String.class, String.class, defaultClass)
                    .newInstance(node, "TabChannel", defaultTrue);
            pluginManager.getClass().getMethod("addPermission", permClass).invoke(pluginManager, permission);
        } catch (Throwable ignored) {
        }
    }
}
