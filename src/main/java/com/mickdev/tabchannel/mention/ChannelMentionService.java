package com.mickdev.tabchannel.mention;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ChannelMentionService {

	private ChannelMentionService() {}

	/**
	 * Traite les @ dans un message (global ou canal) et envoie une notification aux joueurs concernés.
	 *
	 * @param channelId {@code global} pour le chat vanilla, sinon id du canal
	 */
	public static void processMessage(ServerPlayer sender, String channelId, String rawText, Component preview) {
		if (sender == null || rawText == null || rawText.isBlank() || preview == null) {
			return;
		}

		MinecraftServer server = sender.server;
		if (server == null) {
			return;
		}

		String channel = channelId == null || channelId.isBlank() ? "global" : channelId;
		Set<UUID> notified = new HashSet<>();

		List<String> onlineNames = server.getPlayerList().getPlayers().stream()
				.map(p -> p.getGameProfile().getName())
				.toList();

		for (String name : ChannelMentionParser.findPlayerMentions(rawText, onlineNames)) {
			ServerPlayer target = findOnlinePlayer(server, name);
			if (target != null && target.getUUID() != sender.getUUID()) {
				notifyPlayer(target, channel, preview, notified);
			}
		}

        boolean wantsStaff = ChannelMentionParser.containsStaffTagMention(rawText);
		boolean wantsAdmin = ChannelMentionParser.containsAdminTagMention(rawText);

		boolean staffSpamCooldown = false;
		if (wantsStaff || wantsAdmin) {
			ChannelMentionAntiSpam.StaffMentionResult spam =
					ChannelMentionAntiSpam.tryStaffAdminMention(sender.getUUID());
			if (!spam.allowed()) {
				staffSpamCooldown = true;
				sender.sendSystemMessage(Component.translatable(
								"tabchannel.error.mention_staff_cooldown",
								spam.cooldownSecondsLeft())
						.withStyle(ChatFormatting.RED));
			} else {
				for (ServerPlayer target : server.getPlayerList().getPlayers()) {
					if (target.getUUID().equals(sender.getUUID())) {
						continue;
					}
					boolean match = false;
					if (wantsAdmin && ChannelStaffRoles.isAdmin(target)) {
						match = true;
					} else if (wantsStaff && ChannelStaffRoles.isStaff(target)) {
						match = true;
					}
					if (match) {
						notifyPlayer(target, channel, preview, notified);
					}
				}
			}
		}

		if (notified.isEmpty()
				&& !ChannelMentionParser.extractMentionTokens(rawText).isEmpty()
				&& !staffSpamCooldown) {
			sender.sendSystemMessage(Component.translatable(
							"tabchannel.info.mention_no_ping_self_or_offline"
					).withStyle(ChatFormatting.YELLOW));
		}
	}

	private static ServerPlayer findOnlinePlayer(MinecraftServer server, String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	private static void notifyPlayer(ServerPlayer target, String channelId, Component preview, Set<UUID> notified) {
		if (!notified.add(target.getUUID())) {
			return;
		}
		ServerPlayNetworking.send(target, new ChannelMentionNotifyPayload(channelId, preview));
	}
}
