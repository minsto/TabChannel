package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.server.level.ServerPlayer;

public final class BukkitFactionsCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return BukkitDetector.hasPlugin("Factions")
                || BukkitDetector.hasPlugin("SaberFactions")
                || BukkitDetector.hasPlugin("FactionsUUID")
                || BukkitDetector.hasPlugin("MassiveCore");
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        Object faction = getFaction(player);
        if (faction == null) {
            return "";
        }

        String id = callString(faction, "getId");
        if (!id.isBlank()) {
            return "bukkit_" + id;
        }

        String tag = callString(faction, "getTag");
        if (!tag.isBlank()) {
            return "bukkit_" + tag.toLowerCase().replace(" ", "_");
        }

        String name = callString(faction, "getName");
        if (!name.isBlank()) {
            return "bukkit_" + name.toLowerCase().replace(" ", "_");
        }

        return "";
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        Object faction = getFaction(player);
        if (faction == null) {
            return "";
        }

        String tag = callString(faction, "getTag");
        if (!tag.isBlank()) {
            return tag;
        }

        String name = callString(faction, "getName");
        return name == null ? "" : name;
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        Object fPlayer = getFPlayer(player);
        if (fPlayer == null) {
            return false;
        }

        if (callBoolean(fPlayer, "isAdmin")) {
            return true;
        }

        if (callBoolean(fPlayer, "isColeader")) {
            return true;
        }

        if (callBoolean(fPlayer, "isModerator")) {
            return true;
        }

        try {
            Object role = fPlayer.getClass().getMethod("getRole").invoke(fPlayer);
            if (role == null) {
                return false;
            }

            String roleName = role.toString().toLowerCase();
            return roleName.contains("admin")
                    || roleName.contains("leader")
                    || roleName.contains("coleader")
                    || roleName.contains("co_leader")
                    || roleName.contains("moderator")
                    || roleName.contains("mod")
                    || roleName.contains("officer");
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean sameFaction(ServerPlayer a, ServerPlayer b) {
        String fa = getFactionId(a);
        String fb = getFactionId(b);
        return !fa.isBlank() && fa.equalsIgnoreCase(fb);
    }

    private Object getFPlayer(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        Object bukkitPlayer = BukkitDetector.getBukkitPlayer(player.getUUID());
        if (bukkitPlayer == null) {
            return null;
        }

        try {
            Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
            Object instance = fPlayersClass.getMethod("getInstance").invoke(null);

            try {
                return instance.getClass()
                        .getMethod("getByPlayer", Class.forName("org.bukkit.entity.Player"))
                        .invoke(instance, bukkitPlayer);
            } catch (NoSuchMethodException ignored) {
                return instance.getClass()
                        .getMethod("getByOfflinePlayer", Class.forName("org.bukkit.OfflinePlayer"))
                        .invoke(instance, bukkitPlayer);
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object getFaction(ServerPlayer player) {
        Object fPlayer = getFPlayer(player);
        if (fPlayer == null) {
            return null;
        }

        try {
            Object faction = fPlayer.getClass().getMethod("getFaction").invoke(fPlayer);
            if (faction == null) {
                return null;
            }

            if (callBoolean(faction, "isWilderness")) {
                return null;
            }

            if (callBoolean(faction, "isNone")) {
                return null;
            }

            return faction;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String callString(Object obj, String method) {
        if (obj == null) {
            return "";
        }

        try {
            Object result = obj.getClass().getMethod(method).invoke(obj);
            return result == null ? "" : result.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static boolean callBoolean(Object obj, String method) {
        if (obj == null) {
            return false;
        }

        try {
            Object result = obj.getClass().getMethod(method).invoke(obj);
            return result instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
