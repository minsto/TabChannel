package com.mickdev.tabchannel.Api.Compact;

import net.minecraft.server.level.ServerPlayer;

public interface FactionCompat {

    boolean isLoaded();

    String getFactionId(ServerPlayer player);

    String getFactionName(ServerPlayer player);

    boolean isLeaderOrOfficer(ServerPlayer player);

    boolean sameFaction(ServerPlayer a, ServerPlayer b);
}