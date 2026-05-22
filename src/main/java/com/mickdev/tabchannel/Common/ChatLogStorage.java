package com.mickdev.tabchannel.Common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ChatLogStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<ChatLogEntry> CACHE = new ArrayList<>();
    private static Path loadedPath = null;

    private ChatLogStorage() {
    }

    public static void log(MinecraftServer server, ServerPlayer player, String channel, String message) {
        if (server == null || player == null || message == null || message.isBlank()) {
            return;
        }

        load(server);

        ChatLogEntry entry = new ChatLogEntry(
                System.currentTimeMillis(),
                player.getUUID(),
                player.getGameProfile().getName(),
                channel == null || channel.isBlank() ? "global" : channel,
                message,
                player.getX(),
                player.getY(),
                player.getZ(),
                player.level().dimension().location().toString()
        );

        CACHE.add(entry);
        save(server);
    }

    public static List<ChatLogEntry> search(MinecraftServer server, String query, int limit) {
        load(server);

        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();

        return CACHE.stream()
                .filter(entry -> q.isBlank()
                        || entry.playerName().toLowerCase(Locale.ROOT).contains(q)
                        || entry.channel().toLowerCase(Locale.ROOT).contains(q)
                        || entry.message().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparingLong(ChatLogEntry::time).reversed())
                .limit(limit)
                .toList();
    }

    public static void load(MinecraftServer server) {
        Path path = getPath(server);

        if (loadedPath != null && loadedPath.equals(path)) {
            return;
        }

        loadedPath = path;
        CACHE.clear();

        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ChatLogEntry[] entries = GSON.fromJson(reader, ChatLogEntry[].class);

            if (entries != null) {
                CACHE.addAll(List.of(entries));
            }
        } catch (Exception e) {
            System.err.println("Failed to load TabChannel chat logs: " + e.getMessage());
        }
    }

    public static void save(MinecraftServer server) {
        Path path = getPath(server);

        try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(CACHE, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save TabChannel chat logs: " + e.getMessage());
        }
    }

    public static void resetCache() {
        CACHE.clear();
        loadedPath = null;
    }

    private static Path getPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("tabchannel")
                .resolve("chat_logs.json");
    }
}