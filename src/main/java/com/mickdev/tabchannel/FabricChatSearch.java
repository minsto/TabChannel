package com.mickdev.tabchannel;

import com.mickdev.tabchannel.Common.ChatClickUtil;
import com.mickdev.tabchannel.Common.ChatLogEntry;
import com.mickdev.tabchannel.Common.ChatLogStorage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class FabricChatSearch {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    private FabricChatSearch() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ArgumentBuilder<CommandSourceStack, ?> searchQuery = Commands.argument("query", StringArgumentType.greedyString())
                    .executes(ctx -> executeSearch(ctx.getSource(), StringArgumentType.getString(ctx, "query")));

            dispatcher.register(
                    Commands.literal("chatsearch")
                            .requires(FabricChatSearch::canUseSearch)
                            .then(searchQuery)
            );

            dispatcher.register(
                    Commands.literal("chat")
                            .requires(FabricChatSearch::canUseSearch)
                            .then(Commands.literal("search").then(searchQuery))
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(Component.literal(
                                        "Usage: /chatsearch <texte>  ou  /chat search <texte>"
                                ).withStyle(ChatFormatting.RED));
                                return 0;
                            })
            );

            dispatcher.register(Commands.literal("tabchanneltploc")
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
        });
    }

    private static boolean canUseSearch(CommandSourceStack source) {
        return source.hasPermission(0);
    }

    private static int executeSearch(CommandSourceStack source, String query) {
        List<ChatLogEntry> results = ChatLogStorage.search(
                source.getServer(),
                query,
                25
        );

        int count = results.size();

        if (count == 0) {
            source.sendSuccess(() ->
                    Component.literal("Chat search: aucun résultat pour \"" + query + "\"")
                            .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        source.sendSuccess(() ->
                Component.literal("Chat search: " + count + " résultat(s)")
                        .withStyle(ChatFormatting.YELLOW), false);

        for (ChatLogEntry entry : results) {
            source.sendSuccess(() -> format(entry), false);
        }

        return count;
    }

    private static Component format(ChatLogEntry entry) {
        String time = FORMAT.format(new Date(entry.time()));

        return Component.literal("[" + time + "] ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("[" + entry.channel() + "] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(entry.playerName() + ": ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(entry.message()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" "))
                .append(ChatClickUtil.tpLocButton(entry));
    }
}
