package com.mickdev.tabchannel.NetWork.CodecChanel;

import com.mickdev.tabchannel.TabChannel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChannelAddTabPayload(
        String id,
        String displayName,
        boolean global,
        boolean selected,
        int page,
        String tabColor,
        boolean staffChannel
) implements CustomPacketPayload {

    public static final Type<ChannelAddTabPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TabChannel.MODID, "channel_add_tab"));

    public static final StreamCodec<ByteBuf, ChannelAddTabPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ChannelAddTabPayload decode(ByteBuf buf) {
                    String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                    String displayName = ByteBufCodecs.STRING_UTF8.decode(buf);
                    boolean global = ByteBufCodecs.BOOL.decode(buf);
                    boolean selected = ByteBufCodecs.BOOL.decode(buf);
                    int page = ByteBufCodecs.VAR_INT.decode(buf);
                    String tabColor = ByteBufCodecs.STRING_UTF8.decode(buf);
                    boolean staffChannel = ByteBufCodecs.BOOL.decode(buf);

                    return new ChannelAddTabPayload(
                            id,
                            displayName,
                            global,
                            selected,
                            page,
                            tabColor,
                            staffChannel
                    );
                }

                @Override
                public void encode(ByteBuf buf, ChannelAddTabPayload payload) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, payload.id());
                    ByteBufCodecs.STRING_UTF8.encode(buf, payload.displayName());
                    ByteBufCodecs.BOOL.encode(buf, payload.global());
                    ByteBufCodecs.BOOL.encode(buf, payload.selected());
                    ByteBufCodecs.VAR_INT.encode(buf, payload.page());
                    ByteBufCodecs.STRING_UTF8.encode(buf, payload.tabColor());
                    ByteBufCodecs.BOOL.encode(buf, payload.staffChannel());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}