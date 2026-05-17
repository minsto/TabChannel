package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import com.mickdev.tabchannel.TabChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelClearMessagesPayload(String channelId) implements CustomPacketPayload {

    public static final Type<ChannelClearMessagesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    TabChannel.MODID,
                    "channel_clear_messages"
            ));

    public static final StreamCodec<FriendlyByteBuf, ChannelClearMessagesPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ChannelClearMessagesPayload::channelId,
                    ChannelClearMessagesPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
