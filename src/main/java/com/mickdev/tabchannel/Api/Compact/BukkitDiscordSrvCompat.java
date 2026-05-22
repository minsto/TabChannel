package com.mickdev.tabchannel.Api.Compact;

public final class BukkitDiscordSrvCompat implements DiscordCompat {

    @Override
    public boolean isLoaded() {
        return BukkitDetector.isBukkitPresent()
                && BukkitDetector.hasPlugin("DiscordSRV");
    }

    @Override
    public void sendToDiscord(String channel, String playerName, String message) {
        try {
            Class<?> apiClass = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Object api = apiClass.getMethod("getPlugin").invoke(null);

            api.getClass()
                    .getMethod("processChatMessage",
                            Class.forName("org.bukkit.entity.Player"),
                            String.class,
                            String.class,
                            boolean.class)
                    .invoke(api, null, "[" + channel + "] " + playerName + ": " + message, channel, false);

        } catch (Throwable ignored) {
        }
    }
}
