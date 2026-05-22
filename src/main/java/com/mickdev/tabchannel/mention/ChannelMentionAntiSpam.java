package com.mickdev.tabchannel.mention;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import com.mickdev.tabchannel.Config;
import net.minecraft.server.MinecraftServer;

public final class ChannelMentionAntiSpam {

	private static final Map<UUID, SpamState> STAFF_ADMIN_MENTIONS = new ConcurrentHashMap<>();

	private ChannelMentionAntiSpam() {
	}

	public record StaffMentionResult(boolean allowed, int cooldownSecondsLeft) {

		public static StaffMentionResult ok() {
			return new StaffMentionResult(true, 0);
		}

		public static StaffMentionResult onCooldown(int secondsLeft) {
			return new StaffMentionResult(false, Math.max(1, secondsLeft));
		}
	}

	private record SpamState(int count, long windowStartMs, long cooldownUntilMs) {
	}

	public static StaffMentionResult tryStaffAdminMention(UUID senderId) {
		if (senderId == null) {
			return StaffMentionResult.ok();
		}

		int max = Config.MENTION_STAFF_ADMIN_MAX;
		int windowSec = Config.MENTION_STAFF_ADMIN_WINDOW_SECONDS;
		int cooldownSec = Config.MENTION_STAFF_ADMIN_COOLDOWN_SECONDS;
		if (max <= 0) {
			return StaffMentionResult.ok();
		}

		long now = System.currentTimeMillis();
		long windowMs = Math.max(1, windowSec) * 1000L;
		long cooldownMs = Math.max(1, cooldownSec) * 1000L;

		SpamState state = STAFF_ADMIN_MENTIONS.get(senderId);

		// Si le joueur est encore en cooldown => bloqué
		if (state != null && state.cooldownUntilMs() > now) {
			int left = (int) Math.ceil((state.cooldownUntilMs() - now) / 1000.0);
			return StaffMentionResult.onCooldown(left);
		}

		// Si aucune donnée, fenêtre expirée, ou cooldown terminé => nouveau cycle
		if (state == null || now - state.windowStartMs() >= windowMs || state.cooldownUntilMs() > 0) {
			STAFF_ADMIN_MENTIONS.put(senderId, new SpamState(1, now, 0L));
			return StaffMentionResult.ok();
		}

		int newCount = state.count() + 1;

		// Si on dépasse la limite => cooldown
		if (newCount > max) {
			long cooldownUntil = now + cooldownMs;
			STAFF_ADMIN_MENTIONS.put(senderId, new SpamState(0, now, cooldownUntil));
			return StaffMentionResult.onCooldown(cooldownSec);
		}

		// Sinon on augmente le compteur
		STAFF_ADMIN_MENTIONS.put(senderId, new SpamState(newCount, state.windowStartMs(), 0L));
		return StaffMentionResult.ok();
	}

	public static void clear(UUID playerId) {
		if (playerId != null) {
			STAFF_ADMIN_MENTIONS.remove(playerId);
		}
	}

	public static void clearAll() {
		STAFF_ADMIN_MENTIONS.clear();
	}

	public static void clearServer(MinecraftServer server) {
		if (server == null) {
			return;
		}

		server.getPlayerList().getPlayers().forEach(player ->
				STAFF_ADMIN_MENTIONS.remove(player.getUUID())
		);
	}
}