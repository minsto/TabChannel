package com.mickdev.tabchannel.Common.ChatColors;

import com.mickdev.tabchannel.ChannelPermissionResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ChatStyleFormatter {

    private static final Pattern HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private ChatStyleFormatter() {
    }

    public static Component format(ServerPlayer player, String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Component.empty();
        }

        String[] parts = rawText.trim().split("\\s+");
        Style style = Style.EMPTY;

        int index = 0;

        while (index < parts.length && parts[index].startsWith("#")) {
            TagResult result = applyTag(player, style, parts[index]);

            if (!result.accepted()) {
                break;
            }

            style = result.style();
            index++;
        }

        StringBuilder message = new StringBuilder();

        for (int i = index; i < parts.length; i++) {
            if (!message.isEmpty()) {
                message.append(" ");
            }
            message.append(parts[i]);
        }

        return Component.literal(message.toString()).withStyle(style);
    }

    private static TagResult applyTag(ServerPlayer player, Style style, String tag) {
        String lower = tag.toLowerCase(Locale.ROOT);

        switch (lower) {
            case "#bold" -> {
                return canUseFormat(player) ? ok(style.withBold(true)) : denied();
            }
            case "#italic" -> {
                return canUseFormat(player) ? ok(style.withItalic(true)) : denied();
            }
            case "#underline", "#underlined" -> {
                return canUseFormat(player) ? ok(style.withUnderlined(true)) : denied();
            }
            case "#strike", "#strikethrough" -> {
                return canUseFormat(player) ? ok(style.withStrikethrough(true)) : denied();
            }
            case "#obfuscated", "#magic" -> {
                return canUseFormat(player) ? ok(style.withObfuscated(true)) : denied();
            }
        }

        ChatFormatting color = minecraftColor(lower);

        if (color != null) {
            return canUseColor(player) ? ok(style.withColor(color)) : denied();
        }

        if (HEX.matcher(tag).matches()) {
            if (!canUseHex(player)) {
                return denied();
            }

            int rgb = Integer.parseInt(tag.substring(1), 16);
            return ok(style.withColor(TextColor.fromRgb(rgb)));
        }

        return denied();
    }

    private static TagResult ok(Style style) {
        return new TagResult(true, style);
    }

    private static TagResult denied() {
        return new TagResult(false, Style.EMPTY);
    }

    private record TagResult(boolean accepted, Style style) {
    }

    private static ChatFormatting minecraftColor(String tag) {
        return switch (tag) {
            case "#black" -> ChatFormatting.BLACK;
            case "#dark_blue" -> ChatFormatting.DARK_BLUE;
            case "#dark_green" -> ChatFormatting.DARK_GREEN;
            case "#dark_aqua" -> ChatFormatting.DARK_AQUA;
            case "#dark_red" -> ChatFormatting.DARK_RED;
            case "#dark_purple" -> ChatFormatting.DARK_PURPLE;
            case "#gold" -> ChatFormatting.GOLD;
            case "#gray", "#grey" -> ChatFormatting.GRAY;
            case "#dark_gray", "#dark_grey" -> ChatFormatting.DARK_GRAY;
            case "#blue" -> ChatFormatting.BLUE;
            case "#green" -> ChatFormatting.GREEN;
            case "#aqua", "#cyan" -> ChatFormatting.AQUA;
            case "#red" -> ChatFormatting.RED;
            case "#light_purple", "#pink", "#purple" -> ChatFormatting.LIGHT_PURPLE;
            case "#yellow" -> ChatFormatting.YELLOW;
            case "#white" -> ChatFormatting.WHITE;
            default -> null;
        };
    }

    private static boolean canUseColor(ServerPlayer player) {
        return player.hasPermissions(0)
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.color")
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.style");
    }

    private static boolean canUseHex(ServerPlayer player) {
        return player.hasPermissions(0)
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.hex")
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.style");
    }

    private static boolean canUseFormat(ServerPlayer player) {
        return player.hasPermissions(0)
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.format")
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.chat.style");
    }
}