package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;



import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelMessagePayload(
        String channelId,
        Component message
) implements CustomPacketPayload {

    public static final Type<ChannelMessagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    TabChannel.MODID,
                    "channel_message"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelMessagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ChannelMessagePayload::channelId,
                    ComponentSerialization.STREAM_CODEC,
                    ChannelMessagePayload::message,
                    ChannelMessagePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
