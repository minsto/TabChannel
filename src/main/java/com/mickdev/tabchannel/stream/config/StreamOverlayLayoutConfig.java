package com.mickdev.tabchannel.stream.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StreamOverlayLayoutConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int MIN_WIDTH = 180;
    public static final int MIN_HEIGHT = 80;
    public static final int DEFAULT_WIDTH = 340;
    public static final int DEFAULT_HEIGHT = 140;
    public static final float DEFAULT_OPACITY = 0.65F;
    public static final int DEFAULT_MARGIN = 10;
    public static final int DEFAULT_TOP_Y = 40;

    public boolean visible = false;
    public int x = -1;
    public int y = DEFAULT_TOP_Y;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;
    public float opacity = DEFAULT_OPACITY;

    public static StreamOverlayLayoutConfig INSTANCE = new StreamOverlayLayoutConfig();

    private StreamOverlayLayoutConfig() {
    }

    public static int x() {
        return INSTANCE.x;
    }

    public static int y() {
        return INSTANCE.y;
    }

    public static int width() {
        return INSTANCE.width;
    }

    public static int height() {
        return INSTANCE.height;
    }

    public static float opacity() {
        return INSTANCE.opacity;
    }

    public static boolean visible() {
        return INSTANCE.visible;
    }

    public static void setVisible(boolean value) {
        INSTANCE.visible = value;
        save();
    }

    public static void toggleVisible() {
        setVisible(!INSTANCE.visible);
    }

    public static void setPosition(int newX, int newY) {
        INSTANCE.x = Math.max(0, newX);
        INSTANCE.y = Math.max(0, newY);
        save();
    }

    public static void setSize(int newWidth, int newHeight) {
        INSTANCE.width = Math.max(MIN_WIDTH, newWidth);
        INSTANCE.height = Math.max(MIN_HEIGHT, newHeight);
        save();
    }

    public static void setOpacity(float value) {
        INSTANCE.opacity = Math.max(0.15F, Math.min(1.0F, value));
        save();
    }

    public static void resetSize() {
        INSTANCE.width = DEFAULT_WIDTH;
        INSTANCE.height = DEFAULT_HEIGHT;
        save();
    }

    public static void resetPosition() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : 800;
        INSTANCE.x = Math.max(DEFAULT_MARGIN, screenW - INSTANCE.width - DEFAULT_MARGIN);
        INSTANCE.y = DEFAULT_TOP_Y;
        save();
    }

    public static void bootstrap() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return;
        }

        load(mc.gameDirectory.toPath().resolve("config"));
        ensureDefaultPosition();
    }

    public static void ensureDefaultPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        if (INSTANCE.x < 0) {
            INSTANCE.x = Math.max(DEFAULT_MARGIN, screenW - INSTANCE.width - DEFAULT_MARGIN);
        }

        if (INSTANCE.y <= 0) {
            INSTANCE.y = DEFAULT_TOP_Y;
        }

        if (INSTANCE.x + INSTANCE.width > screenW) {
            INSTANCE.x = Math.max(0, screenW - INSTANCE.width - DEFAULT_MARGIN);
        }

        if (INSTANCE.y + INSTANCE.height > screenH - 48) {
            INSTANCE.y = Math.max(0, screenH - INSTANCE.height - 48);
        }

        validate();
        save();
    }

    public static void load(Path configDir) {
        Path path = getPath(configDir);

        try {
            if (!Files.exists(path)) {
                INSTANCE = new StreamOverlayLayoutConfig();
                writeToFile(path);
                return;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                StreamOverlayLayoutConfig loaded = GSON.fromJson(reader, StreamOverlayLayoutConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
            }

            validate();
        } catch (Exception e) {
            INSTANCE = new StreamOverlayLayoutConfig();
        }
    }

    public static void save() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameDirectory == null) {
            return;
        }

        validate();
        writeToFile(getPath(mc.gameDirectory.toPath().resolve("config")));
    }

    private static void validate() {
        INSTANCE.width = Math.max(MIN_WIDTH, INSTANCE.width);
        INSTANCE.height = Math.max(MIN_HEIGHT, INSTANCE.height);
        INSTANCE.opacity = Math.max(0.15F, Math.min(1.0F, INSTANCE.opacity));
    }

    private static Path getPath(Path configDir) {
        return configDir.resolve("tabchannel").resolve("stream_overlay.json");
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
}
