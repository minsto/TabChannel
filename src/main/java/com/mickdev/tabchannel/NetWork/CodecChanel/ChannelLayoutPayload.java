package com.mickdev.tabchannel.NetWork.CodecChanel;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelLayoutPayload(String action) implements CustomPacketPayload {

    public static final Type<ChannelLayoutPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "channel_layout"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelLayoutPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ChannelLayoutPayload::action,
                    ChannelLayoutPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}