package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MpBrowseModePayload(boolean browseAllOnline) implements CustomPacketPayload {

    public static final Type<MpBrowseModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "mp_browse_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MpBrowseModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MpBrowseModePayload::browseAllOnline,
                    MpBrowseModePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
