package com.mickdev.tabchannel.Common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.Locale;

public final class ChatClickUtil {

    private ChatClickUtil() {
    }

    public static Component tpLocButton(ChatLogEntry entry) {

        String dimension = entry.dimension();

        if (dimension.contains(":")) {
            dimension = dimension.substring(dimension.indexOf(":") + 1);
        }

        String command = "/tabchanneltploc "
                + dimension + " "
                + ((int) entry.x()) + " "
                + ((int) entry.y()) + " "
                + ((int) entry.z());

        return Component.literal("[TP LOC]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                command
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Teleport to logged location")
                        )));
    }
}