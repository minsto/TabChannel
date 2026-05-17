package com.mickdev.tabchannel.NetWork.CodecChanel;


import com.mickdev.tabchannel.TabChannel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelSelectTabPayload(String channelId) implements CustomPacketPayload {

    public static final Type<ChannelSelectTabPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "channel_select_tab"));

    public static final StreamCodec<ByteBuf, ChannelSelectTabPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ChannelSelectTabPayload::channelId,
                    ChannelSelectTabPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
