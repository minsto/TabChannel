package com.mickdev.tabchannel.mixin;

import com.mickdev.tabchannel.ChannelChatService;
import com.mickdev.tabchannel.ChatManager;
import com.mickdev.tabchannel.ChatTabState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerSystemMessageMixin {

    @Unique
    private static final ThreadLocal<Boolean> TabChannel_REDIRECTING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void tabchannel$redirectSystemMessageToChannel(Component message, CallbackInfo ci) {
        if (TabChannel_REDIRECTING.get()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;

        ChatTabState state = ChatManager.getState(player.getUUID());
        String channelId = state.getSelectedChannelId();

        if (channelId == null || channelId.isBlank() || "global".equals(channelId)) {
            return;
        }

        try {
            TabChannel_REDIRECTING.set(true);
            ChannelChatService.pushSystemToChannel(player, channelId, message);
            ci.cancel();
        } finally {
            TabChannel_REDIRECTING.set(false);
        }
    }
}
