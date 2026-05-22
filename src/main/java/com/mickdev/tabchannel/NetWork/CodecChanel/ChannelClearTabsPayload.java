package com.mickdev.tabchannel.NetWork.CodecChanel;



import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelClearTabsPayload() implements CustomPacketPayload {

    public static final Type<ChannelClearTabsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "channel_clear_tabs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelClearTabsPayload> STREAM_CODEC =
            StreamCodec.unit(new ChannelClearTabsPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
