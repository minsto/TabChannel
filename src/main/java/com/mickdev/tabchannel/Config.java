package com.mickdev.tabchannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {

    public static int MENTION_STAFF_ADMIN_MAX = 3;
    public static int MENTION_STAFF_ADMIN_WINDOW_SECONDS = 60;
    public static int MENTION_STAFF_ADMIN_COOLDOWN_SECONDS = 30;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("tabchannel.json");

    public int mentionStaffAdminMax = 3;
    public int mentionStaffAdminWindowSeconds = 60;
    public int mentionStaffAdminCooldownSeconds = 30;

    public static Config INSTANCE = new Config();

    private Config() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Config loaded = GSON.fromJson(reader, Config.class);

            if (loaded != null) {
                INSTANCE = loaded;
                INSTANCE.validate();
            }
        } catch (Exception e) {
            TabChannel.LOGGER.error("Failed to load config.", e);
            INSTANCE = new Config();
            save();
        }

        MENTION_STAFF_ADMIN_MAX = INSTANCE.mentionStaffAdminMax;
        MENTION_STAFF_ADMIN_WINDOW_SECONDS = INSTANCE.mentionStaffAdminWindowSeconds;
        MENTION_STAFF_ADMIN_COOLDOWN_SECONDS = INSTANCE.mentionStaffAdminCooldownSeconds;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            TabChannel.LOGGER.error("Failed to save config.", e);
        }
    }

    private void validate() {
        mentionStaffAdminMax = clamp(mentionStaffAdminMax, 1, 3);
        mentionStaffAdminWindowSeconds = clamp(mentionStaffAdminWindowSeconds, 10, 3600);
        mentionStaffAdminCooldownSeconds = clamp(mentionStaffAdminCooldownSeconds, 5, 600);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}