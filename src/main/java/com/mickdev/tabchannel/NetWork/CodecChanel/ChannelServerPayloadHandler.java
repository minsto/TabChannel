package com.mickdev.tabchannel.NetWork.CodecChanel;


import com.mickdev.tabchannel.*;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class ChannelServerPayloadHandler {

    private ChannelServerPayloadHandler() {
    }

    public static void handleSelectTab(ChannelSelectTabPayload payload, ServerPlayer player) {
        if (player == null) {
            return;
        }
        {

            String channelId = ChatManager.sanitizeId(payload.channelId());
            ChatChannel channel = ChatManager.getChannel(channelId);

            if (channel == null) {
                return;
            }

            boolean bypass = player.hasPermissions(2)
                    || ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_JOIN)
                    || ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_PRIVATE);

            try {
                ChatManager.joinChannel(channel, player, bypass);
            } catch (Exception e) {
                return;
            }

            ChatManager.selectTab(player.getUUID(), channelId);
            ChannelSyncService.syncPlayer(player);
            ChannelNetworking.sendChannelHistory(player, channelId);
            // Message de join dans le chat du canal
            ChannelChatService.pushSystemToChannel(
                    player,
                    channelId,
                    Component.translatable(
                            "tabchannel.success.joined_channel",
                            channel.getDisplayName()
                    ).withStyle(ChatFormatting.GREEN)
            );

// Règle du canal dans le chat du canal
            String rule = null;
            try {
                rule = channel.getRules();
            } catch (Exception ignored) {
            }

            if (rule != null && !rule.isBlank()) {
                ChannelChatService.pushSystemToChannel(
                        player,
                        channelId,
                        Component.translatable(
                                "tabchannel.info.channel_rules",
                                rule
                        ).withStyle(ChatFormatting.YELLOW)
                );
            }

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS,
                    0.7F,
                    1.0F
            );
        }
    }

    public static void handleChangePage(ChannelChangePagePayload payload, ServerPlayer player) {
        if (player == null) {
            return;
        }
        {

            if (payload.next()) {
                ChatManager.nextPage(player.getUUID());
            } else {
                ChatManager.prevPage(player.getUUID());
            }

            ChannelSyncService.syncPlayer(player);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS,
                    0.7F,
                    1.0F
            );
        }
    }

    public static void handleSendMessage(ChannelSendMessagePayload payload, ServerPlayer player) {
        if (player == null) {
            return;
        }
        {

            String channelId = payload.channelId();
            String message = payload.message();

            if (message == null) {
                return;
            }

            message = message.trim();
            if (message.isEmpty()) {
                return;
            }

            ChannelChatService.handleChannelMessage(player, channelId, message);
        }
    }
}
