package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class SoleaFactionCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        String teamId = getTeamId(player);
        return teamId.isBlank() ? "" : "solea_" + teamId;
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        try {
            Object data = getSoleaData(player);
            if (data == null) {
                return "";
            }

            String teamId = getRawTeamId(data, player.getUUID());
            if (teamId.isBlank()) {
                return "";
            }

            Map<?, ?> teams = getMap(data, "getTeams");
            Object rawTeam = teams.get(teamId);

            if (!(rawTeam instanceof CompoundTag tag)) {
                return "";
            }

            String name = tag.getString("name");
            return name == null ? "" : name;
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        try {
            Object data = getSoleaData(player);
            if (data == null) {
                return false;
            }

            Map<?, ?> ranks = getMap(data, "getPlayerTeamRanks");
            Object rankObj = ranks.get(player.getUUID());

            if (rankObj == null) {
                return false;
            }

            String rank = rankObj.toString().toLowerCase();

            return rank.equals("leader")
                    || rank.equals("manager")
                    || rank.equals("officer")
                    || rank.equals("admin");
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

    private String getTeamId(ServerPlayer player) {
        try {
            Object data = getSoleaData(player);
            if (data == null) {
                return "";
            }

            return getRawTeamId(data, player.getUUID());
        } catch (Throwable ignored) {
            return "";
        }
    }

    private String getRawTeamId(Object data, UUID uuid) {
        try {
            Map<?, ?> playerTeams = getMap(data, "getPlayerTeams");
            Object teamId = playerTeams.get(uuid);
            return teamId == null ? "" : teamId.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private Object getSoleaData(ServerPlayer player) {
        try {
            Class<?> dataClass = Class.forName("com.soleapixel.soleaapi.data.SoleaSavedData");
            Method getMethod = dataClass.getMethod("get", net.minecraft.server.MinecraftServer.class);
            return getMethod.invoke(null, player.server);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Map<?, ?> getMap(Object data, String methodName) {
        try {
            Object result = data.getClass().getMethod(methodName).invoke(data);

            if (result instanceof Map<?, ?> map) {
                return map;
            }
        } catch (Throwable ignored) {
        }

        return Map.of();
    }
}
