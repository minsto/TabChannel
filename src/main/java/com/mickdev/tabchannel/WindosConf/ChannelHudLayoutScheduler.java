package com.mickdev.tabchannel.WindosConf;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Opens channel HUD layout screens after chat closes (same issue as stream overlay). */
public final class ChannelHudLayoutScheduler {

    private static final int OPEN_DELAY_TICKS = 3;

    private static Screen pending;
    private static int ticksRemaining;

    private ChannelHudLayoutScheduler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ChannelHudLayoutScheduler::tick);
    }

    public static void openPosition() {
        schedule(new ChannelPositionScreen());
    }

    public static void openResize() {
        schedule(new ChannelResizeScreen());
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
