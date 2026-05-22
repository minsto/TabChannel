package com.mickdev.tabchannel.Common;

import java.util.UUID;

public record ChatLogEntry(
        long time,
        UUID uuid,
        String playerName,
        String channel,
        String message,
        double x,
        double y,
        double z,
        String dimension
) {
}
