package com.mickdev.tabchannel;

import com.mickdev.tabchannel.Common.ChatClickUtil;
import com.mickdev.tabchannel.Common.ChatLogEntry;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
@EventBusSubscriber(modid = TabChannel.MODID)
public final class NeoForgeChatSearch {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    private NeoForgeChatSearch() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chatsearch")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("query", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String query = StringArgumentType.getString(ctx, "query");

                            List<ChatLogEntry> results = ChatLogStorage.search(
                                    ctx.getSource().getServer(),
                                    query,
                                    25
                            );

                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Chat search results: " + results.size())
                                            .withStyle(ChatFormatting.YELLOW), false);

                            for (ChatLogEntry entry : results) {
                                ctx.getSource().sendSuccess(() -> format(entry), false);
                            }

                            return results.size();
                        })));

        event.getDispatcher().register(Commands.literal("tabchanneltploc")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    String dimension = StringArgumentType.getString(ctx, "dimension");
                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                    double z = DoubleArgumentType.getDouble(ctx, "z");

                                                    ResourceKey<Level> key = ResourceKey.create(
                                                            Registries.DIMENSION,
                                                            ResourceLocation.parse(dimension)
                                                    );

                                                    var level = ctx.getSource().getServer().getLevel(key);

                                                    if (level == null) {
                                                        ctx.getSource().sendFailure(Component.literal("Dimension not found."));
                                                        return 0;
                                                    }

                                                    var player = ctx.getSource().getPlayerOrException();

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

    private static Component format(ChatLogEntry entry) {
        String time = FORMAT.format(new Date(entry.time()));

        return Component.literal("[" + time + "] ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("[" + entry.channel() + "] ")
                        .withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(entry.playerName() + ": ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(entry.message())
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" "))
                .append(ChatClickUtil.tpLocButton(entry));
    }
}