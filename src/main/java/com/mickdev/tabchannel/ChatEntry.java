package com.mickdev.tabchannel;

import net.minecraft.network.chat.Component;

public record ChatEntry(
        String channelId,
        Component raw,
        long time,
        String fingerprint
) {
}