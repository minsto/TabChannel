package com.mickdev.tabchannel.stream.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mickdev.tabchannel.stream.StreamPlatform;
import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StreamChatConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean integrationEnabled = false;
    public boolean overlayEnabled = true;
    public String platform = "TWITCH";
    public String twitchChannel = "";
    public String twitchLogin = "";
    public String twitchToken = "";
    public float overlayOpacity = 0.65F;
    public int messageLimit = 50;
    public boolean compactMode = true;
    public boolean soundsEnabled = false;
    public boolean autoScroll = true;
    public boolean emotesEnabled = false;

    public static StreamChatConfig INSTANCE = new StreamChatConfig();

    private StreamChatConfig() {
    }

    public static boolean integrationEnabled() {
        return INSTANCE.integrationEnabled;
    }

    public static void setIntegrationEnabled(boolean value) {
        INSTANCE.integrationEnabled = value;
        save();
    }

    public static int messageLimit() {
        return Math.max(10, Math.min(200, INSTANCE.messageLimit));
    }

    public static boolean useTwitch() {
        return true;
    }

    public static void bootstrap() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return;
        }

        load(mc.gameDirectory.toPath().resolve("config"));
    }

    public static void reload() {
        bootstrap();
    }

    public static void load(Path configDir) {
        Path path = getPath(configDir);

        try {
            if (!Files.exists(path)) {
                INSTANCE = new StreamChatConfig();
                writeToFile(path);
                return;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                StreamChatConfig loaded = GSON.fromJson(reader, StreamChatConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    INSTANCE.sanitize();
                }
            }
        } catch (Exception e) {
            INSTANCE = new StreamChatConfig();
        }
    }

    private void sanitize() {
        platform = "TWITCH";
        twitchChannel = nullToEmpty(twitchChannel);
        twitchLogin = nullToEmpty(twitchLogin);
        twitchToken = nullToEmpty(twitchToken);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static void save() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return;
        }

        INSTANCE.platform = "TWITCH";
        writeToFile(getPath(mc.gameDirectory.toPath().resolve("config")));
        StreamOverlayLayoutConfig.setOpacity(INSTANCE.overlayOpacity);
    }

    public static Path getConfigPath() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return Path.of("config/tabchannel/stream_chat.json");
        }

        return getPath(mc.gameDirectory.toPath().resolve("config"));
    }

    private static Path getPath(Path configDir) {
        return configDir.resolve("tabchannel").resolve("stream_chat.json");
    }

    private static void writeToFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception ignored) {
        }
    }

    public static StreamPlatform selectedSendPlatform() {
        return StreamPlatform.TWITCH;
    }
}
