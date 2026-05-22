package com.mickdev.tabchannel.client.mixin;

import com.mickdev.tabchannel.stream.gui.StreamChatOverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiStreamOverlayMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void tabchannel$renderStreamHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        StreamChatOverlayRenderer.render(guiGraphics);
    }
}
