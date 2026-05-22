package com.mickdev.tabchannel.Api.Compact;

import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import net.minecraft.server.level.ServerPlayer;

public final class FabricIckerFactionCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        Faction faction = getFaction(player);

        if (faction == null) {
            return "";
        }

        return "icker_" + faction.getID().toString();
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        Faction faction = getFaction(player);

        if (faction == null) {
            return "";
        }

        String name = faction.getName();
        return name == null ? "" : name;
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        User user = getUser(player);

        if (user == null || !user.isInFaction()) {
            return false;
        }

        String rank = user.getRankName();

        if (rank == null) {
            return false;
        }

        rank = rank.toLowerCase();

        return rank.contains("owner")
                || rank.contains("admin")
                || rank.contains("leader")
                || rank.contains("moderator")
                || rank.contains("officer")
                || rank.contains("coleader");
    }

    @Override
    public boolean sameFaction(ServerPlayer a, ServerPlayer b) {
        String fa = getFactionId(a);
        String fb = getFactionId(b);

        return !fa.isBlank() && fa.equalsIgnoreCase(fb);
    }

    private User getUser(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        try {
            return User.get(player.getUUID());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Faction getFaction(ServerPlayer player) {
        User user = getUser(player);

        if (user == null || !user.isInFaction()) {
            return null;
        }

        try {
            return user.getFaction();
        } catch (Throwable ignored) {
            return null;
        }
    }
}