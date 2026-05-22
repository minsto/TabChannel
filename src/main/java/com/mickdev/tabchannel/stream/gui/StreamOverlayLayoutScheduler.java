package com.mickdev.tabchannel.stream.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Opens layout screens a few ticks after /streamchat commands so ChatScreen closing
 * does not cancel {@link Minecraft#setScreen}.
 */
public final class StreamOverlayLayoutScheduler {

    private static final int OPEN_DELAY_TICKS = 3;

    private static Screen pending;
    private static int ticksRemaining;

    private StreamOverlayLayoutScheduler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(StreamOverlayLayoutScheduler::tick);
    }

    public static void openPosition() {
        schedule(new StreamOverlayPositionScreen());
    }

    public static void openResize() {
        schedule(new StreamOverlayResizeScreen());
    }

    private static void schedule(Screen screen) {
        pending = screen;
        ticksRemaining = OPEN_DELAY_TICKS;
    }

    private static void tick(Minecraft client) {
        if (ticksRemaining <= 0 || pending == null) {
            return;
        }

        ticksRemaining--;
        if (ticksRemaining > 0) {
            return;
        }

        Screen screen = pending;
        pending = null;
        client.setScreen(screen);
    }
}
