package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MpMessagePayload(
        String peerName,
        String message,
        boolean incoming
) implements CustomPacketPayload {

    public static final Type<MpMessagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "mp_message"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MpMessagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    MpMessagePayload::peerName,
                    ByteBufCodecs.STRING_UTF8,
                    MpMessagePayload::message,
                    ByteBufCodecs.BOOL,
                    MpMessagePayload::incoming,
                    MpMessagePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
