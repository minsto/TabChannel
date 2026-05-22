package com.mickdev.tabchannel.stream.twitch;

import com.mickdev.tabchannel.stream.StreamChatMessage;
import com.mickdev.tabchannel.stream.StreamChatProvider;
import com.mickdev.tabchannel.stream.StreamPlatform;
import com.mickdev.tabchannel.stream.config.StreamChatConfig;

import java.util.List;

public final class TwitchStreamProvider implements StreamChatProvider {

    private TwitchIrcClient client;
    private volatile String lastError = "";

    @Override
    public StreamPlatform platform() {
        return StreamPlatform.TWITCH;
    }

    @Override
    public void connect() throws Exception {
        String channel = StreamChatConfig.INSTANCE.twitchChannel;
        String login = StreamChatConfig.INSTANCE.twitchLogin;
        String token = StreamChatConfig.INSTANCE.twitchToken;

        if (channel == null || channel.isBlank()) {
            throw new IllegalStateException("Twitch channel is empty");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalStateException("Twitch login is empty");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Twitch token is missing");
        }

        client = new TwitchIrcClient(channel, token, login.trim());
        client.connect();
        lastError = "";
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @Override
    public void sendMessage(String message) throws Exception {
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException("Twitch is not connected");
        }

        client.sendMessage(message);
    }

    @Override
    public List<StreamChatMessage> pollMessages() {
        if (client == null) {
            return List.of();
        }

        lastError = client.lastError();
        return client.drainMessages();
    }

    @Override
    public String getPlatformName() {
        return "Twitch";
    }

    @Override
    public String lastError() {
        return lastError;
    }

}
