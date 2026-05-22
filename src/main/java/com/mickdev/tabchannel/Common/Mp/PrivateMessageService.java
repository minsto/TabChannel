package com.mickdev.tabchannel.Common.Mp;

import com.mickdev.tabchannel.ChannelPermissionResolver;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.MpNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PrivateMessageService {

    private static final Map<UUID, UUID> LAST_REPLY = new HashMap<>();
    private static final Map<UUID, Set<UUID>> BLOCKED = new HashMap<>();
    private static final Set<UUID> SOCIAL_SPY = new HashSet<>();
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final long MP_COOLDOWN_MS = 1200L;

    private PrivateMessageService() {
    }

    public static void send(ServerPlayer sender, ServerPlayer target, String rawMessage) {
        if (sender == null || target == null || rawMessage == null) {
            return;
        }

        String message = rawMessage.trim();

        if (message.isBlank()) {
            error(sender, "tabchannel.mp.error.empty");
            return;
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            error(sender, "tabchannel.mp.error.too_long", MAX_MESSAGE_LENGTH);
            return;
        }

        if (sender.getUUID().equals(target.getUUID())) {
            error(sender, "tabchannel.mp.error.self");
            return;
        }

        if (isBlocked(target, sender)) {
            error(sender, "tabchannel.mp.error.blocked", target.getGameProfile().getName());
            return;
        }

        if (!canBypassCooldown(sender) && isOnCooldown(sender)) {
            error(sender, "tabchannel.mp.error.cooldown");
            return;
        }

        COOLDOWN.put(sender.getUUID(), System.currentTimeMillis());

        LAST_REPLY.put(sender.getUUID(), target.getUUID());
        LAST_REPLY.put(target.getUUID(), sender.getUUID());

        String senderName = sender.getGameProfile().getName();
        String targetName = target.getGameProfile().getName();

        Component senderMsg = Component.translatable("tabchannel.mp.prefix")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable(
                        "tabchannel.mp.you_to",
                        targetName
                ).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        Component targetMsg = Component.translatable("tabchannel.mp.prefix")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable(
                        "tabchannel.mp.from_to_you",
                        senderName
                ).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        sender.sendSystemMessage(senderMsg);
        target.sendSystemMessage(targetMsg);

        MpNetworking.syncMessage(sender, targetName, message, false);
        MpNetworking.syncMessage(target, senderName, message, true);

        target.level().playSound(
                null,
                target.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.6F,
                1.4F
        );

        if (message.toLowerCase(Locale.ROOT).contains("@here")) {
            target.sendSystemMessage(
                    Component.translatable("tabchannel.mp.mention")
                            .withStyle(ChatFormatting.YELLOW)
            );
        }

        logForChatSearch(sender, target, message);
        sendSocialSpy(sender, target, message);
        MpContactRegistry.record(sender, target);
    }

    public static void sendOrQueue(ServerPlayer sender, String targetName, String rawMessage) {
        if (sender == null || targetName == null || targetName.isBlank()) {
            return;
        }

        ServerPlayer target = sender.server.getPlayerList().getPlayerByName(targetName);

        if (target != null) {
            send(sender, target, rawMessage);
            return;
        }

        String message = rawMessage == null ? "" : rawMessage.trim();

        if (message.isBlank()) {
            error(sender, "tabchannel.mp.error.empty");
            return;
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            error(sender, "tabchannel.mp.error.too_long", MAX_MESSAGE_LENGTH);
            return;
        }

        var profile = sender.server.getProfileCache().get(targetName);

        if (profile.isEmpty()) {
            error(sender, "tabchannel.mp.error.offline");
            return;
        }

        UUID receiverId = profile.get().getId();

        if (!MpAccess.canBrowseAllOnlinePlayers(sender) && !MpContactRegistry.hasContact(sender, receiverId)) {
            error(sender, "tabchannel.mp.error.need_contact");
            return;
        }

        MpOfflineMailbox.queue(
                sender.server,
                receiverId,
                targetName,
                sender.getUUID(),
                sender.getGameProfile().getName(),
                message
        );

        String senderName = sender.getGameProfile().getName();

        Component senderMsg = Component.translatable("tabchannel.mp.prefix")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable("tabchannel.mp.you_to", targetName)
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        sender.sendSystemMessage(senderMsg);
        MpNetworking.syncMessage(sender, targetName, message, false);

        logForChatSearchOffline(sender, targetName, message);

        sender.sendSystemMessage(
                Component.translatable("tabchannel.mp.queued", targetName)
                        .withStyle(ChatFormatting.YELLOW)
        );

        MpContactRegistry.record(sender.server, sender.getUUID(), receiverId);
    }

    public static void deliverStoredMessage(ServerPlayer receiver, String senderName, String message) {
        if (receiver == null || senderName == null || senderName.isBlank() || message == null || message.isBlank()) {
            return;
        }

        Component targetMsg = Component.translatable("tabchannel.mp.prefix")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable("tabchannel.mp.from_to_you", senderName)
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        receiver.sendSystemMessage(targetMsg);
        MpNetworking.syncMessage(receiver, senderName, message, true);

        receiver.level().playSound(
                null,
                receiver.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.6F,
                1.4F
        );

        receiver.server.getProfileCache().get(senderName).ifPresent(profile ->
                MpContactRegistry.record(receiver.server, receiver.getUUID(), profile.getId())
        );
    }

    public static void reply(ServerPlayer sender, String message) {
        if (sender == null) {
            return;
        }

        UUID targetId = LAST_REPLY.get(sender.getUUID());

        if (targetId == null) {
            error(sender, "tabchannel.mp.error.no_reply");
            return;
        }

        ServerPlayer target = sender.server.getPlayerList().getPlayer(targetId);

        if (target == null) {
            error(sender, "tabchannel.mp.error.offline");
            return;
        }

        send(sender, target, message);
    }

    public static void notifyOffline(ServerPlayer sender) {
        error(sender, "tabchannel.mp.error.offline");
    }

    public static void notifyNeedContact(ServerPlayer sender) {
        error(sender, "tabchannel.mp.error.need_contact");
    }

    public static void block(ServerPlayer player, ServerPlayer target) {
        if (player == null || target == null) {
            return;
        }

        if (player.getUUID().equals(target.getUUID())) {
            error(player, "tabchannel.mp.error.self");
            return;
        }

        BLOCKED.computeIfAbsent(player.getUUID(), id -> new HashSet<>()).add(target.getUUID());

        player.sendSystemMessage(
                Component.translatable(
                        "tabchannel.mp.blocked_player",
                        target.getGameProfile().getName()
                ).withStyle(ChatFormatting.RED)
        );
    }

    public static void unblock(ServerPlayer player, ServerPlayer target) {
        if (player == null || target == null) {
            return;
        }

        BLOCKED.computeIfAbsent(player.getUUID(), id -> new HashSet<>()).remove(target.getUUID());

        player.sendSystemMessage(
                Component.translatable(
                        "tabchannel.mp.unblocked_player",
                        target.getGameProfile().getName()
                ).withStyle(ChatFormatting.GREEN)
        );
    }

    public static void toggleSocialSpy(ServerPlayer player) {
        if (player == null) {
            return;
        }

        if (!player.hasPermissions(2)
                && !ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.mp.socialspy")) {
            error(player, "tabchannel.mp.error.permission");
            return;
        }

        if (SOCIAL_SPY.remove(player.getUUID())) {
            player.sendSystemMessage(
                    Component.translatable("tabchannel.mp.socialspy.disabled")
                            .withStyle(ChatFormatting.RED)
            );
        } else {
            SOCIAL_SPY.add(player.getUUID());
            player.sendSystemMessage(
                    Component.translatable("tabchannel.mp.socialspy.enabled")
                            .withStyle(ChatFormatting.GREEN)
            );
        }
    }

    public static boolean isBlocked(ServerPlayer receiver, ServerPlayer sender) {
        if (receiver == null || sender == null) {
            return false;
        }

        return BLOCKED.getOrDefault(receiver.getUUID(), Set.of()).contains(sender.getUUID());
    }

    private static boolean isOnCooldown(ServerPlayer player) {
        long last = COOLDOWN.getOrDefault(player.getUUID(), 0L);
        return System.currentTimeMillis() - last < MP_COOLDOWN_MS;
    }

    private static boolean canBypassCooldown(ServerPlayer player) {
        return player.hasPermissions(2)
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.mp.bypasscooldown");
    }

    private static void sendSocialSpy(ServerPlayer sender, ServerPlayer target, String message) {
        Component spy = Component.translatable("tabchannel.mp.socialspy.prefix")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable(
                        "tabchannel.mp.socialspy.line",
                        sender.getGameProfile().getName(),
                        target.getGameProfile().getName()
                ).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        for (ServerPlayer staff : sender.server.getPlayerList().getPlayers()) {
            if (!SOCIAL_SPY.contains(staff.getUUID())) {
                continue;
            }

            if (staff.getUUID().equals(sender.getUUID())
                    || staff.getUUID().equals(target.getUUID())) {
                continue;
            }

            staff.sendSystemMessage(spy);
        }
    }

    private static void logForChatSearch(ServerPlayer sender, ServerPlayer target, String message) {
        logForChatSearchOffline(sender, target.getGameProfile().getName(), message);
    }

    private static void logForChatSearchOffline(ServerPlayer sender, String targetName, String message) {
        String logMessage = sender.getGameProfile().getName()
                + " -> "
                + targetName
                + ": "
                + message;

        ChatLogStorage.log(
                sender.server,
                sender,
                "mp",
                logMessage
        );
    }

    private static void error(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(
                Component.translatable(key, args).withStyle(ChatFormatting.RED)
        );
    }
}
