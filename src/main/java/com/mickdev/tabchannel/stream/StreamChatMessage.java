package com.mickdev.tabchannel.stream;

public record StreamChatMessage(
        StreamPlatform platform,
        String username,
        String displayName,
        String message,
        int colorArgb,
        String badgeText,
        long timestampMs,
        boolean moderator,
        boolean subscriber,
        boolean broadcaster
) {
    public String formattedName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        return username == null || username.isBlank() ? "?" : username;
    }
}
