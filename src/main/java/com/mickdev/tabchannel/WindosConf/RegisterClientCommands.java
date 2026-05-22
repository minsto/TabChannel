package com.mickdev.tabchannel.WindosConf;
import com.mickdev.tabchannel.TabChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(
        modid = TabChannel.MODID,
        value = Dist.CLIENT
)
public final class RegisterClientCommands {

    private RegisterClientCommands() {
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("channel")

                        .then(Commands.literal("resize")

                                .executes(ctx -> {

                                    Minecraft.getInstance().setScreen(
                                            new ChannelResizeScreen()
                                    );

                                    return 1;
                                })

                                .then(Commands.literal("default")

                                        .executes(ctx -> {

                                            Minecraft mc = Minecraft.getInstance();

                                            ChannelHudLayoutConfig.resetSize();

                                            ChannelHudLayoutConfig.resetPosition(
                                                    mc.getWindow().getGuiScaledHeight()
                                            );

                                            return 1;
                                        }))
                        )

                        .then(Commands.literal("position")

                                .executes(ctx -> {

                                    Minecraft.getInstance().setScreen(
                                            new ChannelPositionScreen()
                                    );

                                    return 1;
                                })

                                .then(Commands.literal("default")

                                        .executes(ctx -> {

                                            Minecraft mc = Minecraft.getInstance();

                                            ChannelHudLayoutConfig.resetPosition(
                                                    mc.getWindow().getGuiScaledHeight()
                                            );

                                            return 1;
                                        }))
                        )
        );
    }
}