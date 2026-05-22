package com.mickdev.tabchannel.Commandes;


import com.mickdev.tabchannel.*;
import com.mickdev.tabchannel.Api.Compact.CompatServices;
import com.mickdev.tabchannel.Common.Mp.PrivateMessageService;
import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelLayoutPayload;
import com.mickdev.tabchannel.WindosConf.ChannelHudLayoutConfig;
import com.mickdev.tabchannel.WindosConf.ChannelPositionScreen;
import com.mickdev.tabchannel.WindosConf.ChannelResizeScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;


public final class ChannelCommands {

    /** Niveau 0 = tous les joueurs (solo, LAN, Youer sans LuckPerms). */
    private static final int COMMAND_PERMISSION_LEVEL = 0;

    private ChannelCommands() {
    }

    private static LiteralArgumentBuilder<CommandSourceStack> rootLiteral(String name) {
        return Commands.literal(name)
                .requires(source -> source.hasPermission(COMMAND_PERMISSION_LEVEL));
    }
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(createChannelRoot("channel"));
        dispatcher.register(createChannelRoot("ch"));
        dispatcher.register(createChannelRoot("tabchannel"));
        dispatcher.register(createChannelRoot("tc"));
        dispatcher.register(createChannelRoot("tb"));

        dispatcher.register(createCreateChannel());
        dispatcher.register(createJoinChannel());
        dispatcher.register(createChannelList());
        dispatcher.register(createSetChannel());
        dispatcher.register(Commands.literal("mp")
                .then(Commands.literal("block")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    PrivateMessageService.block(
                                            ctx.getSource().getPlayerOrException(),
                                            EntityArgument.getPlayer(ctx, "player")
                                    );
                                    return 1;
                                })))
                .then(Commands.literal("unblock")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    PrivateMessageService.unblock(
                                            ctx.getSource().getPlayerOrException(),
                                            EntityArgument.getPlayer(ctx, "player")
                                    );
                                    return 1;
                                })))
                .then(Commands.literal("socialspy")
                        .executes(ctx -> {
                            PrivateMessageService.toggleSocialSpy(
                                    ctx.getSource().getPlayerOrException()
                            );
                            return 1;
                        }))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    PrivateMessageService.send(
                                            ctx.getSource().getPlayerOrException(),
                                            EntityArgument.getPlayer(ctx, "player"),
                                            StringArgumentType.getString(ctx, "message")
                                    );
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("r")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            PrivateMessageService.reply(
                                    ctx.getSource().getPlayerOrException(),
                                    StringArgumentType.getString(ctx, "message")
                            );
                            return 1;
                        })));
        dispatcher.register(Commands.literal("tabchanneltploc")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    String dimension = StringArgumentType.getString(ctx, "dimension");

                                                    if (!dimension.contains(":")) {
                                                        dimension = "minecraft:" + dimension;
                                                    }

                                                    ResourceKey<Level> key = ResourceKey.create(
                                                            Registries.DIMENSION,
                                                            ResourceLocation.parse(dimension)
                                                    );

                                                    ServerLevel level = ctx.getSource().getServer().getLevel(key);

                                                    if (level == null) {
                                                        ctx.getSource().sendFailure(
                                                                Component.literal("Dimension not found: " + dimension)
                                                        );
                                                        return 0;
                                                    }

                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                    double z = DoubleArgumentType.getDouble(ctx, "z");

                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                                                    player.teleportTo(
                                                            level,
                                                            x,
                                                            y,
                                                            z,
                                                            player.getYRot(),
                                                            player.getXRot()
                                                    );

                                                    return 1;
                                                }))))));
    }

    private static Component msg(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    private static boolean isAdmin(ServerPlayer player) {
        return player != null && (
                player.hasPermissions(2)
                        || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.admin")
        );
    }

    private static boolean isStaff(ServerPlayer player) {
        return player != null && (
                isAdmin(player)
                        || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.staff")
        );
    }
    private static LiteralArgumentBuilder<CommandSourceStack> createChannelRoot(String name) {
        return rootLiteral(name)
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(ctx -> executeCreateChannel(
                                                ctx.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(ctx, "name"),
                                                StringArgumentType.getString(ctx, "type"),
                                                ctx.getSource()
                                        )))))

                .then(Commands.literal("join")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> executeJoinChannel(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))

                .then(Commands.literal("list")
                        .executes(ctx -> executeChannelList(ctx.getSource())))

                .then(Commands.literal("delete")
                        .then(Commands.argument("channel", StringArgumentType.greedyString())
                                .executes(ctx -> executeDeleteChannel(ctx.getSource(), StringArgumentType.getString(ctx, "channel")))))

                .then(Commands.literal("color")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .executes(ctx -> executeTabColor(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "channel"),
                                                StringArgumentType.getString(ctx, "color")
                                        )))))

                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("channel", StringArgumentType.greedyString())
                                        .executes(ctx -> executeInvite(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "channel")
                                        ))))).then(Commands.literal("resize")
                        .executes(ctx -> {
                            ServerPlayNetworking.send(
                                    ctx.getSource().getPlayerOrException(),
                                    new ChannelLayoutPayload("resize")
                            );
                            return 1;
                        })
                        .then(Commands.literal("default")
                                .executes(ctx -> {
                                    ServerPlayNetworking.send(
                                            ctx.getSource().getPlayerOrException(),
                                            new ChannelLayoutPayload("resize_default")
                                    );
                                    return 1;
                                })))

                .then(Commands.literal("position")
                        .executes(ctx -> {
                            ServerPlayNetworking.send(
                                    ctx.getSource().getPlayerOrException(),
                                    new ChannelLayoutPayload("position")
                            );
                            return 1;
                        })
                        .then(Commands.literal("default")
                                .executes(ctx -> {
                                    ServerPlayNetworking.send(
                                            ctx.getSource().getPlayerOrException(),
                                            new ChannelLayoutPayload("position_default")
                                    );
                                    return 1;
                                })))

                .then(Commands.literal("leave")
                        .then(Commands.argument("channel", StringArgumentType.greedyString())
                                .executes(ctx -> executeLeaveChannel(ctx.getSource(), StringArgumentType.getString(ctx, "channel")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCreateChannel() {
        return rootLiteral("channelcreate")
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("type", StringArgumentType.word())
                                .executes(ctx -> executeCreateChannel(
                                        ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "name"),
                                        StringArgumentType.getString(ctx, "type"),
                                        ctx.getSource()
                                ))));
    }

    private static boolean canManageChannel(ServerPlayer player, ChatChannel channel, String permissionNode) {
        if (player == null || channel == null) {
            return false;
        }

        if (isAdmin(player) || ChannelPermissionResolver.hasGlobalPerm(player, permissionNode)) {
            return true;
        }

        if (player.getUUID().equals(channel.getOwner())) {
            return true;
        }

        ChannelMemberData data = channel.getMember(player.getUUID());
        return data != null && data.hasPermission(permissionNode);
    }

    private static int executeCreateChannel(ServerPlayer player, String rawName, String rawType, CommandSourceStack source) {
        String name = rawName == null ? "" : rawName.trim();
        String type = rawType == null ? "" : rawType.trim().toLowerCase();

        ChannelVisibility visibility = switch (type) {
            case "public" -> ChannelVisibility.PUBLIC;
            case "private", "prive" -> ChannelVisibility.PRIVATE;
            case "faction" -> ChannelVisibility.FACTION;
            default -> null;
        };

        if (visibility == null) {
            source.sendFailure(msg("tabchannel.error.invalid_type", ChatFormatting.RED));
            return 0;
        }

        if (name.isBlank()) {
            source.sendFailure(msg("tabchannel.error.invalid_channel_name", ChatFormatting.RED));
            return 0;
        }

        if (name.equalsIgnoreCase("staff") && !isAdmin(player)) {
            source.sendFailure(msg("tabchannel.error.staff_channel_admin_only", ChatFormatting.RED));
            return 0;
        }

        if (visibility == ChannelVisibility.FACTION) {
            return executeCreateFactionChannel(player, source);
        }

        try {
            ChatChannel channel = ChatManager.createChannel(name, visibility, player);

            if (name.equalsIgnoreCase("staff")) {
                channel.setStaffChannel(true);
                channel.setTabColor(isAdmin(player) ? "RED" : "WHITE");
            } else {
                channel.setTabColor("WHITE");
            }

            ChannelSavedData.get(player.serverLevel()).setDirty();
            ChannelSyncService.syncPlayer(player);

            source.sendSuccess(
                    () -> msg("tabchannel.success.channel_created", ChatFormatting.GREEN, channel.getDisplayName()),
                    false
            );
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int executeCreateFactionChannel(ServerPlayer player, CommandSourceStack source) {
        String factionId = CompatServices.FACTIONS.getFactionId(player);
        String factionName = CompatServices.FACTIONS.getFactionName(player);

        if (factionId == null || factionId.isBlank()) {
            source.sendFailure(msg("tabchannel.error.must_be_in_faction", ChatFormatting.RED));
            return 0;
        }

        boolean bypass = isAdmin(player)
                || ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.FACTION);

        if (!bypass && !CompatServices.FACTIONS.isLeaderOrOfficer(player)) {
            source.sendFailure(msg("tabchannel.error.faction_owner_or_officer_only", ChatFormatting.RED));
            return 0;
        }

        String factionChannelId = "faction_" + ChatManager.sanitizeId(factionId);
        ChatChannel existing = ChatManager.getChannel(factionChannelId);

        if (existing != null) {
            ChatManager.joinAndSelect(player.getUUID(), existing.getId());
            ChannelSyncService.syncPlayer(player);

            source.sendSuccess(
                    () -> msg("tabchannel.info.faction_channel_exists", ChatFormatting.YELLOW, existing.getDisplayName()),
                    false
            );
            return 1;
        }

        String displayName = factionName == null || factionName.isBlank()
                ? "Faction"
                : "Faction " + factionName;

        try {
            ChatChannel channel = ChatManager.createChannel(displayName, ChannelVisibility.FACTION, player);
            channel.setFactionId(factionId);
            channel.setTabColor("GOLD");

            ChatManager.joinAndSelect(player.getUUID(), channel.getId());
            ChannelSavedData.get(player.serverLevel()).setDirty();
            ChannelSyncService.syncPlayer(player);

            source.sendSuccess(
                    () -> msg("tabchannel.success.faction_channel_created", ChatFormatting.GREEN, channel.getDisplayName()),
                    false
            );
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createJoinChannel() {
        return rootLiteral("channeljoin")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String name = StringArgumentType.getString(ctx, "name").trim();
                            ChatChannel channel = ChatManager.getChannel(name);

                            if (channel == null) {
                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                return 0;
                            }

                            if (channel.isStaffChannel() && !isStaff(player)) {
                                ctx.getSource().sendFailure(msg("tabchannel.error.staff_channel_only", ChatFormatting.RED));
                                return 0;
                            }

                            boolean bypass = ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_JOIN)
                                    || ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_PRIVATE)
                                    || isAdmin(player);

                            if (channel.getVisibility() == ChannelVisibility.FACTION && !bypass) {
                                String playerFactionId = CompatServices.FACTIONS.getFactionId(player);

                                if (playerFactionId == null
                                        || playerFactionId.isBlank()
                                        || !channel.isFactionChannel()
                                        || !playerFactionId.equalsIgnoreCase(channel.getFactionId())) {
                                    ctx.getSource().sendFailure(msg("tabchannel.error.no_access_faction_channel", ChatFormatting.RED));
                                    return 0;
                                }
                            }

                            try {
                                ChatManager.joinChannel(channel, player, bypass);
                                ChannelSavedData.get(player.serverLevel()).setDirty();
                                ChannelSyncService.syncPlayer(player);

                                if (!channel.getRules().isBlank()) {
                                    ChannelChatService.pushSystemToActiveChannel(
                                            player,
                                            msg("tabchannel.info.channel_rules", ChatFormatting.YELLOW, channel.getRules())
                                    );
                                }

                                ctx.getSource().sendSuccess(
                                        () -> msg("tabchannel.success.joined_channel", ChatFormatting.GREEN, channel.getDisplayName()),
                                        false
                                );
                                return 1;
                            } catch (IllegalStateException e) {
                                ctx.getSource().sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
                                return 0;
                            }
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createChannelList() {
        return rootLiteral("channellist")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    boolean adminView = ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_PRIVATE)
                            || isAdmin(player);

                    int shown = 0;
                    ChannelChatService.pushSystemToActiveChannel(player, msg("tabchannel.list.title", ChatFormatting.GOLD));

                    for (ChatChannel channel : ChatManager.getChannels()) {
                        if (channel.isOriginalGlobal()) {
                            continue;
                        }

                        if (channel.isStaffChannel() && !isStaff(player)) {
                            continue;
                        }

                        boolean visible = channel.getVisibility() == ChannelVisibility.PUBLIC
                                || channel.isMember(player.getUUID())
                                || adminView;

                        if (!visible) {
                            continue;
                        }

                        shown++;
                        ChannelChatService.pushSystemToActiveChannel(
                                player,
                                msg("tabchannel.list.entry", ChatFormatting.YELLOW, channel.getDisplayName(), channel.getVisibility().name())
                        );
                    }

                    if (shown == 0) {
                        ChannelChatService.pushSystemToActiveChannel(player, msg("tabchannel.list.empty", ChatFormatting.GRAY));
                    }

                    return 1;
                });
    }

    private static String normalizeChannelPermissionInput(String input) {
        if (input == null) {
            return "";
        }

        String raw = input.trim().toLowerCase();

        return switch (raw) {
            case "delete", "del", "remove" -> ChannelPermissions.DELETE;
            case "ban" -> ChannelPermissions.BAN;
            case "kick", "mute" -> ChannelPermissions.KICK;
            case "perm", "grant", "manage" -> ChannelPermissions.PERM;
            case "rules", "rule" -> ChannelPermissions.RULES;
            case "warn", "filter", "antiswear" -> ChannelPermissions.WARN;
            case "invite", "inv" -> ChannelPermissions.INVITE;
            case "tabcolors", "TabColors" -> ChannelPermissions.TabColors;
            case "f", "faction" -> ChannelPermissions.FACTION;
            default -> raw;
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSetChannel() {
        return rootLiteral("setchannel")

                .then(Commands.literal("delete")
                        .then(Commands.argument("channel", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                    ChatChannel channel = ChatManager.getChannel(channelId);

                                    if (channel == null) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                        return 0;
                                    }

                                    if (!canManageChannel(player, channel, ChannelPermissions.DELETE)) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.cannot_delete_channel", ChatFormatting.RED));
                                        return 0;
                                    }

                                    ChatManager.deleteChannel(channelId);
                                    ChannelSavedData.get(player.serverLevel()).setDirty();
                                    ChannelSyncService.syncPlayer(player);

                                    ctx.getSource().sendSuccess(
                                            () -> msg("tabchannel.success.channel_deleted", ChatFormatting.GREEN),
                                            false
                                    );
                                    return 1;

                                })))

                .then(Commands.literal("tabcolors")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer source = ctx.getSource().getPlayerOrException();
                                            String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                            String colorName = StringArgumentType.getString(ctx, "color");

                                            ChatChannel channel = ChatManager.getChannel(channelId);
                                            if (channel == null) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                return 0;
                                            }

                                            ChannelTabColor color = ChannelTabColor.byName(colorName);

                                            if (!ChannelTabColor.canUse(source, color)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.color_admin_only", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (!canManageChannel(source, channel, ChannelPermissions.PERM)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.cannot_change_tab_color", ChatFormatting.RED));
                                                return 0;
                                            }

                                            channel.setTabColor(color.name());
                                            ChannelSavedData.get(source.serverLevel()).setDirty();

                                            for (ServerPlayer p : source.server.getPlayerList().getPlayers()) {
                                                ChannelSyncService.syncPlayer(p);
                                            }

                                            ctx.getSource().sendSuccess(
                                                    () -> msg("tabchannel.success.tab_color_changed", ChatFormatting.GREEN, color.name()),
                                                    false
                                            );
                                            return 1;
                                        }))))

                .then(Commands.literal("ban")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer source = ctx.getSource().getPlayerOrException();
                                            String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            ChatChannel channel = ChatManager.getChannel(channelId);

                                            if (channel == null) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (!canManageChannel(source, channel, ChannelPermissions.BAN)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.cannot_ban_channel", ChatFormatting.RED));
                                                return 0;
                                            }

                                            channel.ban(target.getUUID());
                                            ChatManager.getState(target.getUUID()).closeTab(channelId);
                                            ChannelSavedData.get(source.serverLevel()).setDirty();
                                            ChannelSyncService.syncPlayer(source);
                                            ChannelSyncService.syncPlayer(target);

                                            ctx.getSource().sendSuccess(
                                                    () -> msg("tabchannel.success.player_banned", ChatFormatting.GREEN),
                                                    false
                                            );
                                            return 1;
                                        }))))

                .then(Commands.literal("kick")
                        .then(Commands.argument("channel", StringArgumentType.greedyString())
                                .then(Commands.argument("days", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("hours", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(ctx -> {
                                                                    ServerPlayer source = ctx.getSource().getPlayerOrException();
                                                                    String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                                                    int days = IntegerArgumentType.getInteger(ctx, "days");
                                                                    int hours = IntegerArgumentType.getInteger(ctx, "hours");
                                                                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                                    ChatChannel channel = ChatManager.getChannel(channelId);

                                                                    if (channel == null) {
                                                                        ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                                        return 0;
                                                                    }

                                                                    if (!canManageChannel(source, channel, ChannelPermissions.KICK)) {
                                                                        ctx.getSource().sendFailure(msg("tabchannel.error.cannot_kick_channel", ChatFormatting.RED));
                                                                        return 0;
                                                                    }

                                                                    ChannelMemberData targetData = channel.getMember(target.getUUID());
                                                                    if (targetData == null) {
                                                                        channel.addMember(target.getUUID(), ChannelRole.MEMBER);
                                                                        targetData = channel.getMember(target.getUUID());
                                                                    }

                                                                    long duration = (days * 86400L + hours * 3600L + seconds) * 1000L;
                                                                    targetData.setMutedUntil(System.currentTimeMillis() + duration);

                                                                    ChatManager.getState(target.getUUID()).closeTab(channelId);
                                                                    ChannelSavedData.get(source.serverLevel()).setDirty();
                                                                    ChannelSyncService.syncPlayer(source);
                                                                    ChannelSyncService.syncPlayer(target);

                                                                    ctx.getSource().sendSuccess(
                                                                            () -> msg("tabchannel.success.player_kicked_muted", ChatFormatting.GREEN),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })))))))

                .then(Commands.literal("perm")
                        .then(Commands.literal("add")
                                .then(Commands.argument("channel", StringArgumentType.string())
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("perm", StringArgumentType.greedyString())
                                                        .executes(ctx -> executeChannelPerm(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "channel"),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "perm"),
                                                                true
                                                        ))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("channel", StringArgumentType.string())
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("perm", StringArgumentType.greedyString())
                                                        .executes(ctx -> executeChannelPerm(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "channel"),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "perm"),
                                                                false
                                                        )))))))

                .then(Commands.literal("rules")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer source = ctx.getSource().getPlayerOrException();
                                            String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                            String text = StringArgumentType.getString(ctx, "text");
                                            ChatChannel channel = ChatManager.getChannel(channelId);

                                            if (channel == null) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (channel.isOriginalGlobal()) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.cannot_edit_global_rules", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (!canManageChannel(source, channel, ChannelPermissions.RULES)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.cannot_edit_rules", ChatFormatting.RED));
                                                return 0;
                                            }

                                            channel.setRules(text);
                                            ChannelSavedData.get(source.serverLevel()).setDirty();

                                            ctx.getSource().sendSuccess(
                                                    () -> msg("tabchannel.success.rules_updated", ChatFormatting.GREEN),
                                                    false
                                            );
                                            return 1;
                                        }))))

                .then(Commands.literal("warn")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .then(Commands.argument("enabled", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer source = ctx.getSource().getPlayerOrException();
                                            String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                            String enabled = StringArgumentType.getString(ctx, "enabled");
                                            ChatChannel channel = ChatManager.getChannel(channelId);

                                            if (channel == null) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (!canManageChannel(source, channel, ChannelPermissions.WARN)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.cannot_edit_antiswear", ChatFormatting.RED));
                                                return 0;
                                            }

                                            channel.setAntiSwear(
                                                    "true".equalsIgnoreCase(enabled)
                                                            || "on".equalsIgnoreCase(enabled)
                                                            || "yes".equalsIgnoreCase(enabled)
                                            );

                                            ChannelSavedData.get(source.serverLevel()).setDirty();

                                            ctx.getSource().sendSuccess(
                                                    () -> msg("tabchannel.success.antiswear_state", ChatFormatting.GREEN, String.valueOf(channel.isAntiSwear())),
                                                    false
                                            );
                                            return 1;
                                        }))))

                .then(Commands.literal("f")
                        .then(Commands.argument("channel", StringArgumentType.string())
                                .executes(ctx -> {
                                    ServerPlayer source = ctx.getSource().getPlayerOrException();
                                    String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                    ChatChannel channel = ChatManager.getChannel(channelId);

                                    if (channel == null) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                        return 0;
                                    }

                                    boolean bypass = ChannelPermissionResolver.hasGlobalPerm(source, ChannelPermissions.FACTION)
                                            || isAdmin(source);

                                    if (!bypass && !CompatServices.FACTIONS.isLeaderOrOfficer(source)) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.faction_admin_only", ChatFormatting.RED));
                                        return 0;
                                    }

                                    String factionId = CompatServices.FACTIONS.getFactionId(source);
                                    if (factionId == null || factionId.isBlank()) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.not_in_faction", ChatFormatting.RED));
                                        return 0;
                                    }

                                    channel.setFactionId(factionId);
                                    ChannelSavedData.get(source.serverLevel()).setDirty();

                                    ctx.getSource().sendSuccess(
                                            () -> msg("tabchannel.success.channel_linked_faction", ChatFormatting.GREEN),
                                            false
                                    );
                                    return 1;
                                })))

                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("channel", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer source = ctx.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                            ChatChannel channel = ChatManager.getChannel(channelId);

                                            if (channel == null) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                                return 0;
                                            }

                                            boolean bypass = isAdmin(source)
                                                    || ChannelPermissionResolver.hasGlobalPerm(source, ChannelPermissions.INVITE);

                                            if (channel.isStaffChannel() && !isStaff(target)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.staff_channel_only", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (channel.isFactionChannel() && !bypass) {
                                                String sourceFactionId = CompatServices.FACTIONS.getFactionId(source);
                                                String targetFactionId = CompatServices.FACTIONS.getFactionId(target);

                                                if (sourceFactionId == null
                                                        || sourceFactionId.isBlank()
                                                        || !sourceFactionId.equalsIgnoreCase(channel.getFactionId())
                                                        || !CompatServices.FACTIONS.isLeaderOrOfficer(source)) {
                                                    ctx.getSource().sendFailure(msg("tabchannel.error.faction_invite_owner_or_officer_only", ChatFormatting.RED));
                                                    return 0;
                                                }

                                                if (targetFactionId == null
                                                        || targetFactionId.isBlank()
                                                        || !targetFactionId.equalsIgnoreCase(channel.getFactionId())) {
                                                    ctx.getSource().sendFailure(msg("tabchannel.error.target_not_in_faction", ChatFormatting.RED));
                                                    return 0;
                                                }
                                            } else if (!canManageChannel(source, channel, ChannelPermissions.INVITE)) {
                                                ctx.getSource().sendFailure(msg("tabchannel.error.creator_only_invite", ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (ChatManager.invitePlayer(channelId, target)) {
                                                ChannelSavedData.get(source.serverLevel()).setDirty();
                                                ChannelSyncService.syncPlayer(target);

                                                ctx.getSource().sendSuccess(
                                                        () -> msg("tabchannel.success.player_invited", ChatFormatting.GREEN),
                                                        false
                                                );

                                                ChannelChatService.pushSystemToActiveChannel(
                                                        target,
                                                        msg("tabchannel.success.received_tab", ChatFormatting.GREEN, channel.getDisplayName())
                                                );
                                                return 1;
                                            }

                                            return 0;
                                        }))))

                .then(Commands.literal("leave")
                        .then(Commands.argument("channel", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String channelId = ChatManager.sanitizeId(StringArgumentType.getString(ctx, "channel"));
                                    ChatChannel channel = ChatManager.getChannel(channelId);

                                    if (channel == null) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
                                        return 0;
                                    }

                                    if (channel.isOriginalGlobal()) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.cannot_leave_global", ChatFormatting.RED));
                                        return 0;
                                    }

                                    if (channel.isFactionChannel()) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.cannot_leave_faction", ChatFormatting.RED));
                                        return 0;
                                    }

                                    if (channel.isStaffChannel()) {
                                        ctx.getSource().sendFailure(msg("tabchannel.error.cannot_leave_staff", ChatFormatting.RED));
                                        return 0;
                                    }

                                    ChatManager.leaveChannel(player.getUUID(), channelId);
                                    ChannelSavedData.get(player.serverLevel()).setDirty();
                                    ChannelSyncService.syncPlayer(player);

                                    ctx.getSource().sendSuccess(
                                            () -> msg("tabchannel.success.channel_left", ChatFormatting.GREEN),
                                            false
                                    );
                                    return 1;
                                })));
    }
    private static int executeJoinChannel(CommandSourceStack source, String rawName) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String name = rawName.trim();

        ChatChannel channel = ChatManager.getChannel(name);

        if (channel == null) {
            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
            return 0;
        }

        if (channel.isStaffChannel() && !isStaff(player)) {
            source.sendFailure(msg("tabchannel.error.staff_channel_only", ChatFormatting.RED));
            return 0;
        }

        boolean bypass =
                ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_JOIN)
                        || ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_PRIVATE)
                        || isAdmin(player);

        try {

            ChatManager.joinChannel(channel, player, bypass);

            ChannelSavedData.get(player.serverLevel()).setDirty();

            ChannelSyncService.syncPlayer(player);

            source.sendSuccess(
                    () -> msg("tabchannel.success.joined_channel",
                            ChatFormatting.GREEN,
                            channel.getDisplayName()),
                    false
            );

            return 1;

        } catch (IllegalStateException e) {

            source.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));

            return 0;
        }
    }

    private static int executeChannelList(CommandSourceStack source) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        boolean adminView =
                ChannelPermissionResolver.hasGlobalPerm(player, ChannelPermissions.BYPASS_PRIVATE)
                        || isAdmin(player);

        int shown = 0;

        ChannelChatService.pushSystemToActiveChannel(
                player,
                msg("tabchannel.list.title", ChatFormatting.GOLD)
        );

        for (ChatChannel channel : ChatManager.getChannels()) {

            if (channel.isOriginalGlobal()) {
                continue;
            }

            if (channel.isStaffChannel() && !isStaff(player)) {
                continue;
            }

            boolean visible =
                    channel.getVisibility() == ChannelVisibility.PUBLIC
                            || channel.isMember(player.getUUID())
                            || adminView;

            if (!visible) {
                continue;
            }

            shown++;

            ChannelChatService.pushSystemToActiveChannel(
                    player,
                    msg("tabchannel.list.entry",
                            ChatFormatting.YELLOW,
                            channel.getDisplayName(),
                            channel.getVisibility().name())
            );
        }

        if (shown == 0) {

            ChannelChatService.pushSystemToActiveChannel(
                    player,
                    msg("tabchannel.list.empty", ChatFormatting.GRAY)
            );
        }

        return 1;
    }

    private static int executeDeleteChannel(CommandSourceStack source, String rawChannel) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String channelId = ChatManager.sanitizeId(rawChannel);

        ChatChannel channel = ChatManager.getChannel(channelId);

        if (channel == null) {

            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));

            return 0;
        }

        if (!canManageChannel(player, channel, ChannelPermissions.DELETE)) {

            source.sendFailure(msg("tabchannel.error.cannot_delete_channel", ChatFormatting.RED));

            return 0;
        }

        ChatManager.deleteChannel(channelId);

        ChannelSavedData.get(player.serverLevel()).setDirty();

        ChannelSyncService.syncPlayer(player);

        source.sendSuccess(
                () -> msg("tabchannel.success.channel_deleted", ChatFormatting.GREEN),
                false
        );

        return 1;
    }

    private static int executeTabColor(CommandSourceStack source, String rawChannel, String rawColor) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String channelId = ChatManager.sanitizeId(rawChannel);

        ChatChannel channel = ChatManager.getChannel(channelId);

        if (channel == null) {

            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));

            return 0;
        }

        ChannelTabColor color = ChannelTabColor.byName(rawColor);

        if (!ChannelTabColor.canUse(player, color)) {
            source.sendFailure(msg("tabchannel.error.color_admin_only", ChatFormatting.RED));
            return 0;
        }

        if (!canManageChannel(player, channel, ChannelPermissions.PERM)) {

            source.sendFailure(msg("tabchannel.error.cannot_change_tab_color", ChatFormatting.RED));

            return 0;
        }

        channel.setTabColor(color.name());

        ChannelSavedData.get(player.serverLevel()).setDirty();

        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            ChannelSyncService.syncPlayer(target);
        }

        source.sendSuccess(
                () -> msg("tabchannel.success.tab_color_changed",
                        ChatFormatting.GREEN,
                        color.name()),
                false
        );

        return 1;
    }

    private static int executeInvite(CommandSourceStack source, ServerPlayer target, String rawChannel) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String channelId = ChatManager.sanitizeId(rawChannel);

        ChatChannel channel = ChatManager.getChannel(channelId);

        if (channel == null) {

            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));

            return 0;
        }

        if (!canManageChannel(player, channel, ChannelPermissions.INVITE)) {

            source.sendFailure(msg("tabchannel.error.creator_only_invite", ChatFormatting.RED));

            return 0;
        }

        if (channel.isStaffChannel() && !isStaff(target)) {

            source.sendFailure(msg("tabchannel.error.staff_channel_only", ChatFormatting.RED));

            return 0;
        }

        if (ChatManager.invitePlayer(channelId, target)) {

            ChannelSavedData.get(player.serverLevel()).setDirty();

            ChannelSyncService.syncPlayer(target);

            source.sendSuccess(
                    () -> msg("tabchannel.success.player_invited", ChatFormatting.GREEN),
                    false
            );

            ChannelChatService.pushSystemToActiveChannel(
                    target,
                    msg("tabchannel.success.received_tab",
                            ChatFormatting.GREEN,
                            channel.getDisplayName())
            );

            return 1;
        }

        return 0;
    }

    private static int executeLeaveChannel(CommandSourceStack source, String rawChannel) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String channelId = ChatManager.sanitizeId(rawChannel);

        ChatChannel channel = ChatManager.getChannel(channelId);

        if (channel == null) {

            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));

            return 0;
        }

        if (channel.isOriginalGlobal()) {

            source.sendFailure(msg("tabchannel.error.cannot_leave_global", ChatFormatting.RED));

            return 0;
        }

        if (channel.isFactionChannel()) {

            source.sendFailure(msg("tabchannel.error.cannot_leave_faction", ChatFormatting.RED));

            return 0;
        }

        if (channel.isStaffChannel()) {

            source.sendFailure(msg("tabchannel.error.cannot_leave_staff", ChatFormatting.RED));

            return 0;
        }

        ChatManager.leaveChannel(player.getUUID(), channelId);

        ChannelSavedData.get(player.serverLevel()).setDirty();

        ChannelSyncService.syncPlayer(player);

        source.sendSuccess(
                () -> msg("tabchannel.success.channel_left", ChatFormatting.GREEN),
                false
        );

        return 1;
    }
    private static int executeChannelPerm(
            CommandSourceStack source,
            String rawChannel,
            ServerPlayer target,
            String rawPerm,
            boolean add
    ) throws CommandSyntaxException {

        ServerPlayer player = source.getPlayerOrException();

        String channelId = ChatManager.sanitizeId(rawChannel);
        String perm = normalizeChannelPermissionInput(rawPerm);

        ChatChannel channel = ChatManager.getChannel(channelId);

        if (channel == null) {
            source.sendFailure(msg("tabchannel.error.channel_not_found", ChatFormatting.RED));
            return 0;
        }

        if (!canManageChannel(player, channel, ChannelPermissions.PERM)) {
            source.sendFailure(msg("tabchannel.error.cannot_grant_permissions", ChatFormatting.RED));
            return 0;
        }

        if (!channel.isMember(target.getUUID())) {
            channel.addMember(target.getUUID(), ChannelRole.MEMBER);
        }

        ChannelMemberData member = channel.getMember(target.getUUID());

        if (member == null) {
            source.sendFailure(Component.literal("Member data not found.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (add) {
            member.grant(perm);
            source.sendSuccess(
                    () -> Component.literal("Permission added: " + perm).withStyle(ChatFormatting.GREEN),
                    false
            );
        } else {
            member.revoke(perm);
            source.sendSuccess(
                    () -> Component.literal("Permission removed: " + perm).withStyle(ChatFormatting.YELLOW),
                    false
            );
        }

        ChannelSavedData.get(player.serverLevel()).setDirty();
        ChannelSyncService.syncPlayer(target);

        return 1;
    }
}