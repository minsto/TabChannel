package com.mickdev.tabchannel.NetWork;




import com.mickdev.tabchannel.NetWork.CodecChanel.*;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelClearMessagesPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMentionNotifyPayload;
import com.mickdev.tabchannel.NetWork.CodecChanel.SOPC2.ChannelMessagePayload;
import com.mickdev.tabchannel.TabChannel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;



import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TabChannel.MODID)
public final class ModPayloads {

    private ModPayloads() {}

    // helper client safe
    private static <T> void clientOnly(T packet, IPayloadContext context,
                                       java.util.function.BiConsumer<T, IPayloadContext> handler) {

        context.enqueueWork(() -> {
            // exécuté uniquement côté client
            if (context.player() == null) {
                handler.accept(packet, context);
            }
        });
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Faction


        // Channels
        registrar.playToClient(ChannelClearTabsPayload.TYPE,
                ChannelClearTabsPayload.STREAM_CODEC,
                ChannelClientPayloadHandler::handleClearTabs);

        registrar.playToClient(ChannelAddTabPayload.TYPE,
                ChannelAddTabPayload.STREAM_CODEC,
                ChannelClientPayloadHandler::handleAddTab);
        registrar.playToClient(ChannelClearMessagesPayload.TYPE,
                ChannelClearMessagesPayload.STREAM_CODEC,
                ChannelClientPayloadHandler::handleClearMessages);
        registrar.playToClient(ChannelMessagePayload.TYPE,
                ChannelMessagePayload.STREAM_CODEC,
                ChannelClientPayloadHandler::handleChannelMessage);

        registrar.playToClient(ChannelMentionNotifyPayload.TYPE,
                ChannelMentionNotifyPayload.STREAM_CODEC,
                ChannelClientPayloadHandler::handleMentionNotify);

        registrar.playToServer(ChannelSelectTabPayload.TYPE,
                ChannelSelectTabPayload.STREAM_CODEC,
                ChannelServerPayloadHandler::handleSelectTab);

        registrar.playToServer(ChannelChangePagePayload.TYPE,
                ChannelChangePagePayload.STREAM_CODEC,
                ChannelServerPayloadHandler::handleChangePage);

        registrar.playToServer(ChannelSendMessagePayload.TYPE,
                ChannelSendMessagePayload.STREAM_CODEC,
                ChannelServerPayloadHandler::handleSendMessage);









    }
}