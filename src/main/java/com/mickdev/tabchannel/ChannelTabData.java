package com.mickdev.tabchannel;

public record ChannelTabData(
        String id,
        String displayName,
        boolean global,
        boolean selected
) {
}
