package com.mickdev.tabchannel.stream;

import java.util.List;

public interface StreamChatProvider {

    StreamPlatform platform();

    void connect() throws Exception;

    void disconnect();

    boolean isConnected();

    void sendMessage(String message) throws Exception;

    List<StreamChatMessage> pollMessages();

    String getPlatformName();

    String lastError();
}
