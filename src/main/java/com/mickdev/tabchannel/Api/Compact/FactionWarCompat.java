package com.mickdev.tabchannel.Api.Compact;

import com.amos.factionwar.data.FactionWarSavedData;
import com.amos.factionwar.world.Faction;
import com.amos.factionwar.world.TeamId;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class FactionWarCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        Faction faction = getFaction(player);
        return faction == null ? "" : "factionwar_" + faction.name().toLowerCase();
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        Faction faction = getFaction(player);
        return faction == null ? "" : faction.name();
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        if (player == null) return false;

        try {
            FactionWarSavedData data = FactionWarSavedData.get(player.server);
            Optional<TeamId> teamId = data.getTeam(player.getUUID());

            if (teamId.isEmpty()) {
                return false;
            }

            return data.getStoredTeam(teamId.get())
                    .map(team -> team.owner().equals(player.getUUID()))
                    .orElse(false);
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

    private Faction getFaction(ServerPlayer player) {
        if (player == null) return null;

        try {
            FactionWarSavedData data = FactionWarSavedData.get(player.server);
            return data.getFaction(player.getUUID()).orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
