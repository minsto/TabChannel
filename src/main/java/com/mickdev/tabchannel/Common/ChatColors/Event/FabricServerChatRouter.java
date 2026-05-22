package com.mickdev.tabchannel.Common.ChatColors.Event;

import com.mickdev.tabchannel.Api.Compact.CompatServices;
import com.mickdev.tabchannel.Api.Compact.EmojifulCompat;
import com.mickdev.tabchannel.ChannelChatService;
import com.mickdev.tabchannel.ChatManager;
import com.mickdev.tabchannel.ChatTabState;
import com.mickdev.tabchannel.Common.ChatColors.ChatStyleFormatter;
import com.mickdev.tabchannel.Common.ChatColors.FabricChatMessageTexts;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.mention.ChannelMentionService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public final class FabricServerChatRouter {

    private FabricServerChatRouter() {
    }

    /**
     * @return {@code true} to allow vanilla broadcast, {@code false} to cancel (message handled here)
     */
    public static boolean allowChatMessage(PlayerChatMessage message, ServerPlayer sender) {
        if (sender == null || sender.server == null) {
            return true;
        }

        String rawText = FabricChatMessageTexts.extractRaw(message);
        if (rawText.isBlank() || rawText.startsWith("/")) {
            return true;
        }

        ChatTabState state = ChatManager.getState(sender.getUUID());
        String channelId = state.getSelectedChannelId();
        if (channelId == null || channelId.isBlank()) {
            channelId = "global";
        }

        if ("global".equalsIgnoreCase(channelId)) {
            handleGlobalChat(sender, rawText);
            return false;
        }

        if (ChannelChatService.handleChannelMessage(sender, channelId, rawText)) {
            ChatLogStorage.log(sender.server, sender, channelId, rawText);
        }

        return false;
    }

    private static void handleGlobalChat(ServerPlayer sender, String rawText) {
        MutableComponent line = buildGlobalChatLine(sender, rawText);

        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(line);
        }

        ChannelMentionService.processMessage(sender, "global", rawText, line);
        ChatLogStorage.log(sender.server, sender, "global", rawText);
    }

    static MutableComponent buildGlobalChatLine(ServerPlayer player, String rawText) {
        rawText = EmojifulCompat.formatMessage(rawText);

        String factionName = CompatServices.FACTIONS.getFactionName(player);
        String prefix = CompatServices.PERMISSIONS.getPrefix(player);
        String suffix = CompatServices.PERMISSIONS.getSuffix(player);
        ChatFormatting nameColor = CompatServices.PERMISSIONS.getNameColor(player);
        String playerName = player.getGameProfile().getName();

        MutableComponent full = Component.empty();

        if (prefix != null && !prefix.isBlank()) {
            full.append(Component.literal(prefix + " ").withStyle(ChatFormatting.GRAY));
        }

        full.append(Component.literal(playerName).withStyle(nameColor));

        if (suffix != null && !suffix.isBlank()) {
            full.append(Component.literal(" " + suffix).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (factionName != null && !factionName.isBlank()) {
            full.append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY));
            full.append(Component.literal(factionName).withStyle(ChatFormatting.AQUA));
            full.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        }

        full.append(Component.literal(" : ").withStyle(ChatFormatting.GRAY));
        full.append(ChatStyleFormatter.format(player, rawText));

        return full;
    }
}
