package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MpSendPayload(
        String targetName,
        String message
) implements CustomPacketPayload {

    public static final Type<MpSendPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "mp_send"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MpSendPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    MpSendPayload::targetName,
                    ByteBufCodecs.STRING_UTF8,
                    MpSendPayload::message,
                    MpSendPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
