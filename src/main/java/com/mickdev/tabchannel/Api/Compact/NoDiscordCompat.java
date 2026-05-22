package com.mickdev.tabchannel.Api.Compact;

public final class NoDiscordCompat implements DiscordCompat {

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void sendToDiscord(String channel, String playerName, String message) {
    }
}
