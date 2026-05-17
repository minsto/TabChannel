package com.mickdev.tabchannel.mixin;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void tabchannel$hideVanillaChatWhenCustomChannel(
            GuiGraphics guiGraphics,
            int tickCount,
            int mouseX,
            int mouseY,
            boolean focused,
            CallbackInfo ci
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        String selectedChannelId = ClientChannelTabState.getSelectedChannelId();
        if (selectedChannelId == null || selectedChannelId.isBlank()) {
            selectedChannelId = "global";
        }

        if (!"global".equals(selectedChannelId)) {
            ci.cancel();
        }
    }
}
