package com.mickdev.tabchannel.Common.Mp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class MpOfflineMailbox {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<QueuedMessage> QUEUE = new ArrayList<>();
    private static Path loadedPath;

    private MpOfflineMailbox() {
    }

    public static void queue(
            MinecraftServer server,
            UUID receiverId,
            String receiverName,
            UUID senderId,
            String senderName,
            String message
    ) {
        if (server == null || receiverId == null || senderId == null || message == null || message.isBlank()) {
            return;
        }

        load(server);

        QUEUE.add(new QueuedMessage(
                receiverId,
                receiverName == null ? "" : receiverName,
                senderId,
                senderName == null ? "" : senderName,
                message.trim(),
                System.currentTimeMillis()
        ));

        save(server);
    }

    public static void deliverPending(ServerPlayer receiver) {
        if (receiver == null) {
            return;
        }

        MinecraftServer server = receiver.server;
        load(server);

        UUID receiverId = receiver.getUUID();
        Iterator<QueuedMessage> it = QUEUE.iterator();
        int delivered = 0;

        while (it.hasNext()) {
            QueuedMessage queued = it.next();

            if (!receiverId.equals(queued.receiverId())) {
                continue;
            }

            PrivateMessageService.deliverStoredMessage(receiver, queued.senderName(), queued.message());
            it.remove();
            delivered++;
        }

        if (delivered > 0) {
            save(server);
            receiver.sendSystemMessage(
                    Component.translatable("tabchannel.mp.delivered_offline", delivered)
                            .withStyle(ChatFormatting.GREEN)
            );
        }
    }

    private static void load(MinecraftServer server) {
        Path path = getPath(server);

        if (loadedPath != null && loadedPath.equals(path)) {
            return;
        }

        loadedPath = path;
        QUEUE.clear();

        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            QueuedMessage[] entries = GSON.fromJson(reader, QueuedMessage[].class);

            if (entries != null) {
                for (QueuedMessage entry : entries) {
                    if (entry != null && entry.receiverId() != null && entry.senderId() != null) {
                        QUEUE.add(entry);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TabChannel: failed to load MP mailbox: " + e.getMessage());
        }
    }

    private static void save(MinecraftServer server) {
        Path path = getPath(server);

        try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(QUEUE.toArray(QueuedMessage[]::new), writer);
            }
        } catch (IOException e) {
            System.err.println("TabChannel: failed to save MP mailbox: " + e.getMessage());
        }
    }

    private static Path getPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("tabchannel")
                .resolve("mp_offline_mailbox.json");
    }

    private record QueuedMessage(
            UUID receiverId,
            String receiverName,
            UUID senderId,
            String senderName,
            String message,
            long timeMs
    ) {
    }
}
