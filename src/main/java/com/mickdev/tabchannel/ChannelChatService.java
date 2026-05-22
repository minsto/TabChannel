package com.mickdev.tabchannel;


import com.mickdev.tabchannel.Api.Compact.CompatServices;
import com.mickdev.tabchannel.Common.ChatColors.ChatStyleFormatter;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelNetworking;
import com.mickdev.tabchannel.mention.ChannelMentionAntiSpam;
import com.mickdev.tabchannel.mention.ChannelMentionService;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class ChannelChatService {

    private ChannelChatService() {
    }

    private static Component msg(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    public static boolean handleChannelMessage(ServerPlayer sender, String channelId, String rawText) {
        if (sender == null || rawText == null || rawText.isBlank()) {
            return false;
        }

        if (channelId == null || channelId.isBlank()) {
            ChatTabState state = ChatManager.getState(sender.getUUID());
            channelId = state.getSelectedChannelId();
        }

        if (channelId == null || channelId.isBlank()) {
            channelId = "global";
        }

        if ("global".equalsIgnoreCase(channelId)) {

            return false;
        }

        ChatChannel channel = ChatManager.getChannel(channelId);
        if (channel == null) {
            sender.sendSystemMessage(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
            return false;
        }

        if (channel.isBanned(sender.getUUID())) {
            sender.sendSystemMessage(msg("tabchannel.error.you_are_banned", ChatFormatting.RED));
            return false;
        }

        ChannelMemberData member = channel.getMember(sender.getUUID());
        if (member == null) {
            sender.sendSystemMessage(msg("tabchannel.error.not_member", ChatFormatting.RED));
            return false;
        }

        if (member.isMutedNow()) {
            sender.sendSystemMessage(msg("tabchannel.error.you_are_muted", ChatFormatting.RED));
            return false;
        }

        if (channel.isAntiSwear() && ChannelMessageFilter.containsBlockedWords(rawText)) {
            pushSystemToActiveChannel(
                    sender,
                    msg("tabchannel.error.message_blocked_swear", ChatFormatting.RED)
            );
            return false;
        }

        // Anti-spam pour toutes les mentions avec @
        if (rawText.contains("@")) {
            ChannelMentionAntiSpam.StaffMentionResult result =
                    ChannelMentionAntiSpam.tryStaffAdminMention(sender.getUUID());

            if (!result.allowed()) {
                sender.sendSystemMessage(
                        msg(
                                "tabchannel.error.mention_cooldown",
                                ChatFormatting.RED,
                                result.cooldownSecondsLeft()
                        )

                );
                return true;
            }
        }

        String factionName = CompatServices.FACTIONS.getFactionName(sender);

        String prefix = CompatServices.PERMISSIONS.getPrefix(sender);
        String suffix = CompatServices.PERMISSIONS.getSuffix(sender);
        ChatFormatting nameColor = CompatServices.PERMISSIONS.getNameColor(sender);
        String playerName = sender.getGameProfile().getName();

        MutableComponent full = Component.empty();

        if (prefix != null && !prefix.isBlank()) {
            full.append(Component.literal(prefix + " "));
        }

        full.append(Component.literal(playerName).withStyle(nameColor));

        if (suffix != null && !suffix.isBlank()) {
            full.append(Component.literal(" " + suffix));
        }

        if (factionName != null && !factionName.isBlank()) {
            full.append(Component.literal(" §7[faction " + factionName + "]"));
        }

        full.append(Component.literal("§f: ").withStyle(ChatFormatting.GRAY));
        full.append(ChatStyleFormatter.format(sender, rawText));

        ChatManager.push(channelId, full);

        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            ChatTabState targetState = ChatManager.getState(target.getUUID());

            if (!channelId.equals(targetState.getSelectedChannelId())) {
                continue;
            }

            if (!channel.isMember(target.getUUID())) {
                continue;
            }

            ChannelNetworking.sendChannelMessage(target, channelId, full);
        }

        ChannelMentionService.processMessage(sender, channelId, rawText, full);
        CompatServices.DISCORD.sendToDiscord(
                channelId,
                sender.getGameProfile().getName(),
                rawText
        );
        return true;
    }

    public static void pushSystemToChannel(ServerPlayer viewer, String channelId, Component message) {
        if (viewer == null || channelId == null || channelId.isBlank() || message == null) {
            return;
        }

        ChatManager.push(channelId, message);
        ChannelNetworking.sendChannelMessage(viewer, channelId, message);
    }

    public static void pushSystemToActiveChannel(ServerPlayer player, Component message) {
        if (player == null || message == null) {
            return;
        }

        ChatTabState state = ChatManager.getState(player.getUUID());
        String channelId = state.getSelectedChannelId();

        if (channelId == null || channelId.isBlank() || "global".equalsIgnoreCase(channelId)) {
            player.sendSystemMessage(message);
            return;
        }

        pushSystemToChannel(player, channelId, message);
    }
}