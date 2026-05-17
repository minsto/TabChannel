package com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2;

import com.mickdev.tabchannel.TabChannel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelMentionNotifyPayload(
		String channelId,
		Component preview
) implements CustomPacketPayload {

	public static final Type<ChannelMentionNotifyPayload> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "channel_mention_notify"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ChannelMentionNotifyPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8,
					ChannelMentionNotifyPayload::channelId,
					ComponentSerialization.STREAM_CODEC,
					ChannelMentionNotifyPayload::preview,
					ChannelMentionNotifyPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
