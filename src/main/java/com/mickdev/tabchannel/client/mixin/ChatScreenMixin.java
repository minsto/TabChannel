package com.mickdev.tabchannel.client.mixin;

import com.mickdev.tabchannel.Render.Gui.ChannelChatScreenHook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void tabchannel$renderTabs(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ChannelChatScreenHook.render((ChatScreen)(Object)this, guiGraphics, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tabchannel$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ChannelChatScreenHook.mouseClicked((ChatScreen)(Object)this, mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void tabchannel$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (ChannelChatScreenHook.mouseScrolled((ChatScreen)(Object)this, mouseX, mouseY, verticalAmount)) {
            cir.setReturnValue(true);
        }
    }
}
