package com.mickdev.tabchannel.mention;

import com.mickdev.tabchannel.Api.Compact.CompatServices;
import com.mickdev.tabchannel.ChannelPermissionResolver;

import net.minecraft.server.level.ServerPlayer;

public final class ChannelStaffRoles {

	private ChannelStaffRoles() {}

	public static boolean isAdmin(ServerPlayer player) {
		if (player == null) {
			return false;
		}
		return player.hasPermissions(2)
				|| ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.admin")
				|| CompatServices.PERMISSIONS.hasPermission(player, "tabchannel.admin");
	}

	public static boolean isStaff(ServerPlayer player) {
		if (player == null) {
			return false;
		}
		return isAdmin(player)
				|| ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.staff")
				|| CompatServices.PERMISSIONS.hasPermission(player, "tabchannel.staff");
	}
}
