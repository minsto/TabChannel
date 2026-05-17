package com.mickdev.tabchannel.Render.Gui;

import com.mickdev.tabchannel.Render.Gui.screens.ChannelMainScreen;
import net.minecraft.client.Minecraft;

public final class ChannelClientGui {

    private ChannelClientGui() {
    }

    public static void openMain() {
        Minecraft.getInstance().setScreen(new ChannelMainScreen());
    }
}
