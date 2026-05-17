package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelSendMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.TabChannel;
import com.mickdev.tabchannel.mixin.ChatScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Envoie le texte saisi vers le serveur via {@link ChannelSendMessagePayload} quand un canal autre que
 * {@code global} est sélectionné. Sans cela, seul le chat vanilla part — toujours vu comme « global ».
 */
@EventBusSubscriber(modid = TabChannel.MODID, value = Dist.CLIENT)
public final class ChannelOutgoingChatHandler {

	private ChannelOutgoingChatHandler() {}

	@SubscribeEvent
	public static void onClientChat(ClientChatEvent event) {
		String raw = event.getMessage();
		if (raw == null || raw.isBlank()) {
			return;
		}
		String trimmed = raw.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		// Commandes : laisser le flux vanilla (serveur).
		if (trimmed.startsWith("/")) {
			return;
		}

		String channelId = ClientChannelTabState.getSelectedChannelId();
		if (channelId == null || channelId.isBlank() || "global".equalsIgnoreCase(channelId)) {
			return;
		}

		event.setCanceled(true);
		PacketDistributor.sendToServer(new ChannelSendMessagePayload(channelId, trimmed));

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof ChatScreen chat) {
			((ChatScreenAccessor) chat).tabchannel$getInput().setValue("");
		}
	}
}
