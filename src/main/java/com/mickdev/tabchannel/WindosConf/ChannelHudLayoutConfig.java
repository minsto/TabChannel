package com.mickdev.tabchannel.WindosConf;




import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChannelHudLayoutConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final int DEFAULT_CHAT_X = 4;
    public static final int DEFAULT_CHAT_WIDTH = 320;
    public static final int DEFAULT_CHAT_HEIGHT = 180;
    public static final int DEFAULT_BOTTOM_MARGIN = 38;

    public int chatX = DEFAULT_CHAT_X;
    public int chatY = 0;
    public int chatWidth = DEFAULT_CHAT_WIDTH;
    public int chatHeight = DEFAULT_CHAT_HEIGHT;

    public static ChannelHudLayoutConfig INSTANCE = new ChannelHudLayoutConfig();

    private ChannelHudLayoutConfig() {
    }

    public static int chatX() {
        return INSTANCE.chatX;
    }

    public static int chatY() {
        return INSTANCE.chatY;
    }

    public static int chatWidth() {
        return INSTANCE.chatWidth;
    }

    public static int chatHeight() {
        return INSTANCE.chatHeight;
    }

    public static void setPosition(int x, int y) {
        INSTANCE.chatX = Math.max(0, x);
        INSTANCE.chatY = Math.max(0, y);
        save();
    }

    public static void setSize(int width, int height) {
        INSTANCE.chatWidth = Math.max(160, width);
        INSTANCE.chatHeight = Math.max(60, height);
        save();
    }

    public static void resetSize() {
        INSTANCE.chatWidth = DEFAULT_CHAT_WIDTH;
        INSTANCE.chatHeight = DEFAULT_CHAT_HEIGHT;
        save();
    }

    public static void resetPosition(int screenHeight) {
        INSTANCE.chatX = DEFAULT_CHAT_X;
        INSTANCE.chatY = Math.max(0, screenHeight - DEFAULT_BOTTOM_MARGIN - INSTANCE.chatHeight);
        save();
    }

    /** Charge la config client et applique la position par défaut si besoin. */
    public static void bootstrap() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return;
        }
        load(mc.gameDirectory.toPath().resolve("config"));
        ensureDefaultPosition();
    }

    /**
     * Même logique que {@code /channel position default} : position en bas à gauche,
     * recalculée si jamais initialisée (chatY &lt;= 0) ou hors écran (résolution / nouveau monde).
     */
    public static void ensureDefaultPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        boolean uninitialized = INSTANCE.chatY <= 0;
        boolean offScreen = INSTANCE.chatY + INSTANCE.chatHeight > screenHeight;

        if (uninitialized || offScreen) {
            resetPosition(screenHeight);
        }
    }

    public static void load(Path configDir) {
        Path path = getPath(configDir);

        try {
            if (!Files.exists(path)) {
                INSTANCE = new ChannelHudLayoutConfig();
                writeToFile(path);
                return;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                ChannelHudLayoutConfig loaded = GSON.fromJson(reader, ChannelHudLayoutConfig.class);

                if (loaded != null) {
                    INSTANCE = loaded;
                }
            }

            validate();

        } catch (Exception ignored) {
            INSTANCE = new ChannelHudLayoutConfig();
        }
    }

    public static void save(Path configDir) {
        writeToFile(getPath(configDir));
    }

    public static void save() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameDirectory == null) {
            return;
        }

        Path configDir = mc.gameDirectory.toPath().resolve("config");
        writeToFile(getPath(configDir));
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

    private static Path getPath(Path configDir) {
        return configDir.resolve("tabchannel").resolve("hud_layout.json");
    }

    private static void validate() {
        INSTANCE.chatWidth = Math.max(160, INSTANCE.chatWidth);
        INSTANCE.chatHeight = Math.max(60, INSTANCE.chatHeight);
        INSTANCE.chatX = Math.max(0, INSTANCE.chatX);
        INSTANCE.chatY = Math.max(0, INSTANCE.chatY);
    }
}