package com.mickdev.tabchannel.Api.Compact;

import net.mcreator.odysseyfactionsrise.network.OdysseyFactionsRiseModVariables;
import net.minecraft.server.level.ServerPlayer;

public final class OdysseyFactionCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        if (player == null) return "";

        try {
            OdysseyFactionsRiseModVariables.PlayerVariables vars =
                    player.getData(OdysseyFactionsRiseModVariables.PLAYER_VARIABLES);

            if (vars == null || !vars.In_faction || vars.player_faction <= 0) {
                return "";
            }

            return "odyssey_" + ((int) vars.player_faction);
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        if (player == null) return "";

        try {
            OdysseyFactionsRiseModVariables.PlayerVariables vars =
                    player.getData(OdysseyFactionsRiseModVariables.PLAYER_VARIABLES);

            if (vars == null || !vars.In_faction) {
                return "";
            }

            String name = clean(vars.player_faction_name);

            if (name.isBlank()) {
                name = "Faction " + ((int) vars.player_faction);
            }

            return name;
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        if (player == null) return false;

        try {
            OdysseyFactionsRiseModVariables.PlayerVariables vars =
                    player.getData(OdysseyFactionsRiseModVariables.PLAYER_VARIABLES);

            return vars != null && vars.In_faction && (vars.is_faction_chief || vars.is_faction_officer);
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

    private static String clean(String text) {
        if (text == null) return "";
        return text.replace("\"", "").trim();
    }
}
