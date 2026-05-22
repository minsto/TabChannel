package com.mickdev.tabchannel.client.mixin;

import com.mickdev.tabchannel.NetWork.CodecChanel.ChannelSendMessagePayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenOutgoingMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void tabchannel$sendToSelectedChannel(
            String message,
            boolean addToRecentChat,
            CallbackInfo ci
    ) {
        if (message == null || message.isBlank()) {
            return;
        }

        String trimmed = message.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("/")) {
            return;
        }

        String channelId = ClientChannelTabState.getSelectedChannelId();

        if (channelId == null || channelId.isBlank() || "global".equalsIgnoreCase(channelId)) {
            return;
        }

        ClientPlayNetworking.send(new ChannelSendMessagePayload(channelId, trimmed));

        ci.cancel();
    }
}