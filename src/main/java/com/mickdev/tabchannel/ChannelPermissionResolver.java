package com.mickdev.tabchannel;

import net.minecraft.server.level.ServerPlayer;

public final class ChannelPermissionResolver {

    private ChannelPermissionResolver() {
    }

    public static boolean hasGlobalPerm(ServerPlayer player, String perm) {
        if (player == null || perm == null || perm.isBlank()) {
            return false;
        }
        if (player.hasPermissions(2)) {
            return true;
        }
        return com.mickdev.tabchannel.Api.Compact.CompatServices.PERMISSIONS.hasPermission(player, perm);
    }
}