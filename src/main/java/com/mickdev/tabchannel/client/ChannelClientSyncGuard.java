package com.mickdev.tabchannel.client;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Warns when the server did not sync channel tabs (common on Youer/NeoForge with only the Fabric jar on server).
 */
public final class ChannelClientSyncGuard {

    private static final int CHECK_DELAY_TICKS = 100;

    private static int ticksUntilCheck = -1;
    private static boolean warnedThisSession;

    private ChannelClientSyncGuard() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ticksUntilCheck = CHECK_DELAY_TICKS;
            warnedThisSession = false;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ticksUntilCheck = -1;
            warnedThisSession = false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksUntilCheck < 0 || warnedThisSession || client.player == null) {
                return;
            }

            ticksUntilCheck--;
            if (ticksUntilCheck > 0) {
                return;
            }

            if (!ClientChannelTabState.getTabs().isEmpty()) {
                return;
            }

            warnedThisSession = true;
            client.player.displayClientMessage(
                    Component.translatable("tabchannel.client.no_server_sync"),
                    false
            );
        });
    }
}
