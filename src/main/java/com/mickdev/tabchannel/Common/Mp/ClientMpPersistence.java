package com.mickdev.tabchannel.Common.Mp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientMpPersistence {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String loadedServerKey = "";

    private ClientMpPersistence() {
    }

    public static void loadForCurrentServer() {
        String key = currentServerKey();
        if (key.equals(loadedServerKey)) {
            return;
        }

        loadedServerKey = key;
        ClientMpChatStore.clearMemory();
        ClientMpNotifications.clearMemory();

        Path path = getPath(key);
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data == null) {
                return;
            }

            if (data.unread != null) {
                ClientMpNotifications.loadUnread(data.unread);
            }

            if (data.displayNames != null) {
                ClientMpChatStore.loadDisplayNames(data.displayNames);
            }

            if (data.conversations != null) {
                for (Map.Entry<String, List<LineDto>> entry : data.conversations.entrySet()) {
                    if (entry.getValue() == null) {
                        continue;
                    }

                    for (LineDto dto : entry.getValue()) {
                        if (dto == null || dto.text == null || dto.text.isBlank()) {
                            continue;
                        }

                        ClientMpChatStore.addMessageRaw(
                                entry.getKey(),
                                dto.text,
                                dto.incoming,
                                dto.timeMs > 0 ? dto.timeMs : System.currentTimeMillis(),
                                false
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TabChannel: failed to load MP data: " + e.getMessage());
        }
    }

    public static void saveForCurrentServer() {
        if (loadedServerKey.isBlank()) {
            loadedServerKey = currentServerKey();
        }

        SaveData data = new SaveData();
        data.unread = ClientMpNotifications.exportUnread();
        data.displayNames = ClientMpChatStore.exportDisplayNames();
        data.conversations = new LinkedHashMap<>();

        for (String peerKey : ClientMpChatStore.getAllPeerKeys()) {
            List<ClientMpChatStore.MpLine> lines = ClientMpChatStore.getMessages(peerKey);
            List<LineDto> dtos = new ArrayList<>();

            for (ClientMpChatStore.MpLine line : lines) {
                LineDto dto = new LineDto();
                dto.text = line.text();
                dto.incoming = line.incoming();
                dto.timeMs = line.timeMs();
                dtos.add(dto);
            }

            data.conversations.put(peerKey, dtos);
        }

        Path path = getPath(loadedServerKey);

        try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("TabChannel: failed to save MP data: " + e.getMessage());
        }
    }

    public static void resetServerContext() {
        saveForCurrentServer();
        loadedServerKey = "";
        ClientMpChatStore.clearMemory();
        ClientMpNotifications.clearMemory();
    }

    private static Path getPath(String serverKey) {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("tabchannel")
                .resolve("mp_" + sanitize(serverKey) + ".json");
    }

    private static String sanitize(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    private static String currentServerKey() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hasSingleplayerServer()) {
            return "singleplayer";
        }

        if (mc.getCurrentServer() != null) {
            var server = mc.getCurrentServer();
            return server.ip + "_" + server.name;
        }

        return "unknown";
    }

    private static final class SaveData {
        Map<String, Integer> unread = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();
        Map<String, List<LineDto>> conversations = new LinkedHashMap<>();
    }

    private static final class LineDto {
        String text;
        boolean incoming;
        long timeMs;
    }
}
