package com.mickdev.tabchannel.mention;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelMentionParser {

	private static final String[] STAFF_TAGS = { "staff", "admin", "mod", "modo" };

	/** Pseudo Minecraft après @ (1–16 caractères). */
	private static final Pattern PLAYER_MENTION = Pattern.compile("(?i)(?:^|\\s)@([a-zA-Z0-9_]{1,16})");

	private ChannelMentionParser() {}

	public static List<String> extractMentionTokens(String message) {
		List<String> tokens = new ArrayList<>();
		if (message == null || message.isBlank()) {
			return tokens;
		}
		Matcher matcher = PLAYER_MENTION.matcher(message);
		while (matcher.find()) {
			tokens.add(matcher.group(1));
		}
		return tokens;
	}

	public static boolean isStaffTagToken(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}
		String lower = token.toLowerCase(Locale.ROOT);
		for (String tag : STAFF_TAGS) {
			if (tag.equals(lower)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsStaffTagMention(String message) {
		for (String token : extractMentionTokens(message)) {
			if (isStaffTagToken(token)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsAdminTagMention(String message) {
		for (String token : extractMentionTokens(message)) {
			if ("admin".equalsIgnoreCase(token)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Résout les @pseudo vers les noms exacts des joueurs en ligne (insensible à la casse).
	 */
	public static List<String> findPlayerMentions(String message, Iterable<String> onlineNames) {
		Set<String> found = new LinkedHashSet<>();
		if (message == null || message.isBlank()) {
			return List.of();
		}

		List<String> tokens = extractMentionTokens(message);
		if (tokens.isEmpty()) {
			return List.of();
		}

		for (String token : tokens) {
			if (isStaffTagToken(token)) {
				continue;
			}
			String match = resolveOnlineName(token, onlineNames);
			if (match != null) {
				found.add(match);
			}
		}
		return new ArrayList<>(found);
	}

	private static String resolveOnlineName(String token, Iterable<String> onlineNames) {
		for (String name : onlineNames) {
			if (name != null && name.equalsIgnoreCase(token)) {
				return name;
			}
		}
		return null;
	}
}
