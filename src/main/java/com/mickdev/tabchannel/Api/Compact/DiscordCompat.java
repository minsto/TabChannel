package com.mickdev.tabchannel.Api.Compact;

public interface DiscordCompat {

    boolean isLoaded();

    void sendToDiscord(String channel, String playerName, String message);
}
