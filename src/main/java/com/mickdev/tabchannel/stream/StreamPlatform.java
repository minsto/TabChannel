package com.mickdev.tabchannel.stream;

public enum StreamPlatform {
    TWITCH("Twitch");

    private final String label;

    StreamPlatform(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
