package com.mickdev.tabchannel.Common.Mp;

import com.mickdev.tabchannel.ChannelPermissionResolver;
import com.mickdev.tabchannel.mention.ChannelStaffRoles;
import net.minecraft.server.level.ServerPlayer;

public final class MpAccess {

    public static final String PERM_BROWSE = "tabchannel.mp.browse";

    private MpAccess() {
    }

    /** Admin, staff, modérateur (LuckPerms) : voit tous les joueurs en ligne dans la liste MP. */
    public static boolean canBrowseAllOnlinePlayers(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        return ChannelStaffRoles.isStaff(player)
                || ChannelPermissionResolver.hasGlobalPerm(player, PERM_BROWSE);
    }
}
