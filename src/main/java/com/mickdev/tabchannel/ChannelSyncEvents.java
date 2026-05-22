package com.mickdev.tabchannel;

import com.mickdev.tabchannel.Api.Compact.CompatServices;
import com.mickdev.tabchannel.Api.Compact.EmojifulCompat;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.Common.Mp.MpOfflineMailbox;
import com.mickdev.tabchannel.mention.ChannelMentionService;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TabChannel.MODID)
public final class ChannelSyncEvents {

	private ChannelSyncEvents() {}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ChannelSavedData.get(player.serverLevel());
			ChatManager.rebuildPlayerTabs(player.getUUID());
			ChannelSyncService.syncPlayer(player);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onServerChat(ServerChatEvent event) {
		if (event.isCanceled()) {
			return;
		}

		ServerPlayer player = event.getPlayer();
		if (player == null) {
			return;
		}

		String raw = event.getRawText();
		if (raw == null || raw.isBlank() || raw.trim().startsWith("/")) {
			return;
		}
		raw = raw.trim();

		ChatTabState state = ChatManager.getState(player.getUUID());
		String channelId = state.getSelectedChannelId();
		if (channelId == null || channelId.isBlank()) {
			channelId = "global";
		}

		if ("global".equalsIgnoreCase(channelId)) {

			MutableComponent full = buildChatLine(player, raw);
			event.setCanceled(true);

			for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
				target.sendSystemMessage(full);
			}

			ChannelMentionService.processMessage(player, "global", raw, full);
			ChatLogStorage.log(player.server, player, "global", raw);
			return;
		}

		event.setCanceled(true);
		if (ChannelChatService.handleChannelMessage(player, channelId, raw)) {
			ChatLogStorage.log(player.server, player, channelId, raw);
		}
	}

	private static MutableComponent buildChatLine(ServerPlayer player, String rawText) {

		rawText = EmojifulCompat.formatMessage(rawText);

		String factionName = CompatServices.FACTIONS.getFactionName(player);

		String prefix = CompatServices.PERMISSIONS.getPrefix(player);
		String suffix = CompatServices.PERMISSIONS.getSuffix(player);

		ChatFormatting nameColor =
				CompatServices.PERMISSIONS.getNameColor(player);

		String playerName = player.getGameProfile().getName();

		MutableComponent full = Component.empty();

		// Prefix
		if (prefix != null && !prefix.isBlank()) {
			full.append(
					Component.literal(prefix + " ")
							.withStyle(ChatFormatting.GRAY)
			);
		}

		// Player name
		full.append(
				Component.literal(playerName)
						.withStyle(nameColor)
		);

		// Suffix
		if (suffix != null && !suffix.isBlank()) {
			full.append(
					Component.literal(" " + suffix)
							.withStyle(ChatFormatting.DARK_GRAY)
			);
		}

		// Faction
		if (factionName != null && !factionName.isBlank()) {
			full.append(
					Component.literal(" [")
							.withStyle(ChatFormatting.DARK_GRAY)
			);

			full.append(
					Component.literal(factionName)
							.withStyle(ChatFormatting.AQUA)
			);

			full.append(
					Component.literal("]")
							.withStyle(ChatFormatting.DARK_GRAY)
			);
		}

		// separator
		full.append(
				Component.literal(" : ")
						.withStyle(ChatFormatting.GRAY)
		);

		// message
		full.append(
				Component.literal(rawText)
						.withStyle(ChatFormatting.WHITE)
		);

		return full;
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ChannelSavedData.get(player.serverLevel());
			ChatManager.rebuildPlayerTabs(player.getUUID());
			ChannelSyncService.syncPlayer(player);
		}
	}

	@SubscribeEvent
	public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ChannelSyncService.syncPlayer(player);
		}
	}
}
