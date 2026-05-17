package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.server.level.ServerPlayer;

public final class NoFactionCompat implements FactionCompat {

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public String getFactionId(ServerPlayer player) {
        return null;
    }

    @Override
    public String getFactionName(ServerPlayer player) {
        return null;
    }

    @Override
    public boolean isLeaderOrOfficer(ServerPlayer player) {
        return false;
    }

    @Override
    public boolean sameFaction(ServerPlayer a, ServerPlayer b) {
        return false;
    }
}
