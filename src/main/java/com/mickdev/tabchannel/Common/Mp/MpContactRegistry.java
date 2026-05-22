package com.mickdev.tabchannel.Common.Mp;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MpContactRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, Set<UUID>> CONTACTS = new HashMap<>();
    private static Path loadedPath;

    private MpContactRegistry() {
    }

    public static void record(ServerPlayer a, ServerPlayer b) {
        if (a == null || b == null) {
            return;
        }

        record(a.server, a.getUUID(), b.getUUID());
    }

    public static void record(MinecraftServer server, UUID a, UUID b) {
        if (server == null || a == null || b == null || a.equals(b)) {
            return;
        }

        load(server);
        link(a, b);
        save(server);
    }

    public static boolean hasContact(ServerPlayer sender, ServerPlayer target) {
        if (sender == null || target == null) {
            return false;
        }

        return hasContact(sender.server, sender.getUUID(), target.getUUID());
    }

    public static boolean hasContact(ServerPlayer sender, UUID targetId) {
        if (sender == null || targetId == null) {
            return false;
        }

        return hasContact(sender.server, sender.getUUID(), targetId);
    }

    public static boolean hasContactByName(ServerPlayer sender, String targetName) {
        if (sender == null || targetName == null || targetName.isBlank()) {
            return false;
        }

        Optional<UUID> targetId = sender.server.getProfileCache().get(targetName).map(profile -> profile.getId());

        return targetId.isPresent() && hasContact(sender, targetId.get());
    }

    private static boolean hasContact(MinecraftServer server, UUID senderId, UUID targetId) {
        load(server);
        Set<UUID> contacts = CONTACTS.get(senderId);
        return contacts != null && contacts.contains(targetId);
    }

    private static void link(UUID a, UUID b) {
        CONTACTS.computeIfAbsent(a, ignored -> new HashSet<>()).add(b);
        CONTACTS.computeIfAbsent(b, ignored -> new HashSet<>()).add(a);
    }

    private static void load(MinecraftServer server) {
        Path path = getPath(server);

        if (loadedPath != null && loadedPath.equals(path)) {
            return;
        }

        loadedPath = path;
        CONTACTS.clear();

        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ContactSave data = GSON.fromJson(reader, ContactSave.class);

            if (data != null && data.links != null) {
                for (ContactLink link : data.links) {
                    if (link != null && link.a != null && link.b != null) {
                        link(link.a, link.b);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TabChannel: failed to load MP contacts: " + e.getMessage());
        }
    }

    private static void save(MinecraftServer server) {
        Path path = getPath(server);
        ContactSave data = new ContactSave();

        Set<String> seen = new HashSet<>();

        for (Map.Entry<UUID, Set<UUID>> entry : CONTACTS.entrySet()) {
            UUID a = entry.getKey();

            for (UUID b : entry.getValue()) {
                String key = a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;

                if (!seen.add(key)) {
                    continue;
                }

                data.links.add(new ContactLink(a, b));
            }
        }

        try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("TabChannel: failed to save MP contacts: " + e.getMessage());
        }
    }

    private static Path getPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("tabchannel")
                .resolve("mp_contacts.json");
    }

    private static final class ContactSave {
        Set<ContactLink> links = new HashSet<>();
    }

    private record ContactLink(UUID a, UUID b) {
    }
}
