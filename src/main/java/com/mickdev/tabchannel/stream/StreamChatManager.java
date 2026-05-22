package com.mickdev.tabchannel.stream;

import com.mickdev.tabchannel.stream.config.StreamChatConfig;
import com.mickdev.tabchannel.stream.twitch.TwitchStreamProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StreamChatManager {

    private static final CopyOnWriteArrayList<StreamChatMessage> MESSAGES = new CopyOnWriteArrayList<>();
    private static final Map<StreamPlatform, StreamChatProvider> PROVIDERS = new EnumMap<>(StreamPlatform.class);
    private static final AtomicBoolean POLLING = new AtomicBoolean(false);
    private static ScheduledExecutorService executor;
    private static volatile Component statusComponent = Component.empty();

    static {
        PROVIDERS.put(StreamPlatform.TWITCH, new TwitchStreamProvider());
    }

    private StreamChatManager() {
    }

    public static void bootstrap() {
        StreamChatConfig.bootstrap();
        startPolling();
    }

    public static void shutdown() {
        disconnectAll();
        stopPolling();
        MESSAGES.clear();
    }

    public static List<StreamChatMessage> getMessages() {
        return List.copyOf(MESSAGES);
    }

    public static void clearMessages() {
        MESSAGES.clear();
    }

    public static Component statusComponent() {
        return statusComponent;
    }

    public static String statusMessage() {
        return statusComponent.getString();
    }

    public static void addTestMessage(StreamPlatform platform, String user, String text) {
        addMessage(new StreamChatMessage(
                platform,
                user,
                user,
                text,
                platform == StreamPlatform.TWITCH ? 0xFFB080FF : 0xFFFF6666,
                user.contains("mod") ? "[MOD]" : "",
                System.currentTimeMillis(),
                user.toLowerCase().contains("mod"),
                user.toLowerCase().contains("sub"),
                false
        ));
    }

    public static void addTestBatch() {
        addTestMessage(StreamPlatform.TWITCH, "StreamerFan", "Hello from Twitch test!");
        addTestMessage(StreamPlatform.TWITCH, "mod_helper", "Welcome to the stream");
        statusComponent = Component.translatable("tabchannel.stream.status.test_added");
    }

    public static void connectAll() {
        if (!StreamChatConfig.integrationEnabled()) {
            statusComponent = Component.translatable("tabchannel.stream.status.integration_off");
            return;
        }

        List<String> errors = new ArrayList<>();

        try {
            connectProvider(StreamPlatform.TWITCH, errors);
        } catch (Exception e) {
            errors.add("Twitch: " + safeError(e));
        }

        statusComponent = errors.isEmpty()
                ? Component.translatable("tabchannel.stream.status.connected_twitch")
                : Component.literal(String.join(" | ", errors));
    }

    public static void disconnectAll() {
        for (StreamChatProvider provider : PROVIDERS.values()) {
            provider.disconnect();
        }

        statusComponent = Component.translatable("tabchannel.stream.status.disconnected");
    }

    public static void connectTwitch() {
        if (!StreamChatConfig.integrationEnabled()) {
            statusComponent = Component.translatable("tabchannel.stream.status.integration_off");
            return;
        }

        List<String> errors = new ArrayList<>();
        try {
            connectProvider(StreamPlatform.TWITCH, errors);
        } catch (Exception e) {
            errors.add("Twitch: " + safeError(e));
        }

        statusComponent = errors.isEmpty()
                ? Component.translatable("tabchannel.stream.status.connected_twitch")
                : Component.literal(String.join(" | ", errors));
    }

    public static void disconnectTwitch() {
        StreamChatProvider provider = provider(StreamPlatform.TWITCH);
        if (provider != null) {
            provider.disconnect();
        }
        statusComponent = Component.translatable("tabchannel.stream.status.disconnected");
    }

    public static boolean isTwitchConnected() {
        StreamChatProvider provider = provider(StreamPlatform.TWITCH);
        return provider != null && provider.isConnected();
    }

    public static boolean isAnyConnected() {
        for (StreamChatProvider provider : PROVIDERS.values()) {
            if (provider.isConnected()) {
                return true;
            }
        }

        return false;
    }

    public static void sendToSelectedPlatform(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        StreamPlatform platform = StreamChatConfig.selectedSendPlatform();
        StreamChatProvider provider = provider(platform);

        if (provider == null) {
            statusComponent = Component.translatable("tabchannel.stream.status.no_provider", platform.label());
            return;
        }

        if (!provider.isConnected()) {
            addMessage(new StreamChatMessage(
                    platform,
                    localPlayerName(),
                    localPlayerName(),
                    text,
                    0xFF55FF88,
                    "",
                    System.currentTimeMillis(),
                    false,
                    false,
                    false
            ));
            statusComponent = Component.translatable("tabchannel.stream.status.local_only", platform.label());
            return;
        }

        ensurePolling();
        ScheduledExecutorService exec = executor;
        if (exec == null) {
            statusComponent = Component.translatable("tabchannel.stream.status.internal_error");
            return;
        }

        String trimmed = text.trim();
        exec.execute(() -> {
            try {
                provider.sendMessage(trimmed);
                Minecraft.getInstance().execute(() -> {
                    echoSentMessage(platform, trimmed);
                    statusComponent = Component.translatable("tabchannel.stream.status.sent", platform.label());
                    notifyPlayer(Component.translatable("tabchannel.stream.sent_feedback", platform.label()));
                });
            } catch (Exception e) {
                String err = safeError(e);
                Minecraft.getInstance().execute(() -> {
                    statusComponent = Component.translatable("tabchannel.stream.status.send_failed", err);
                    notifyPlayer(Component.translatable("tabchannel.stream.send_failed_feedback", err));
                });
            }
        });
    }

    private static void echoSentMessage(StreamPlatform platform, String text) {
        String author = twitchDisplayName();
        boolean owner = platform == StreamPlatform.TWITCH
                && author.equalsIgnoreCase(StreamChatConfig.INSTANCE.twitchChannel);

        addMessage(new StreamChatMessage(
                platform,
                author,
                author,
                text,
                0xFF55FF88,
                owner ? "[OWNER]" : "",
                System.currentTimeMillis(),
                false,
                false,
                owner
        ));
    }

    private static String twitchDisplayName() {
        String login = StreamChatConfig.INSTANCE.twitchLogin;
        if (login != null && !login.isBlank()) {
            return login;
        }
        return localPlayerName();
    }

    private static void notifyPlayer(Component message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(message, false);
        }
    }

    private static void addMessage(StreamChatMessage message) {
        MESSAGES.add(message);
        trimMessages();
    }

    private static void trimMessages() {
        int limit = StreamChatConfig.messageLimit();

        while (MESSAGES.size() > limit) {
            if (!MESSAGES.isEmpty()) {
                MESSAGES.remove(0);
            }
        }
    }

    private static void ensurePolling() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }

        startPolling();
    }

    private static synchronized void startPolling() {
        if (executor != null && !executor.isShutdown()) {
            POLLING.set(true);
            return;
        }

        stopPolling();

        POLLING.set(true);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TabChannel-StreamPoll");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(StreamChatManager::pollProvidersSafe, 0, 200, TimeUnit.MILLISECONDS);
    }

    private static synchronized void stopPolling() {
        POLLING.set(false);

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private static void pollProvidersSafe() {
        try {
            for (StreamChatProvider provider : PROVIDERS.values()) {
                if (!provider.isConnected()) {
                    continue;
                }

                for (StreamChatMessage message : provider.pollMessages()) {
                    Minecraft.getInstance().execute(() -> addMessage(message));
                }

                String err = provider.lastError();
                if (err != null && !err.isBlank()) {
                    statusComponent = Component.translatable("tabchannel.stream.status.provider_error",
                            provider.getPlatformName(), err);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String localPlayerName() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? "You" : player.getGameProfile().getName();
    }

    private static void connectProvider(StreamPlatform platform, List<String> errors) throws Exception {
        StreamChatProvider provider = provider(platform);
        if (provider == null) {
            errors.add(platform.label() + ": internal error (provider missing)");
            return;
        }
        provider.connect();
    }

    private static StreamChatProvider provider(StreamPlatform platform) {
        return PROVIDERS.get(platform);
    }

    private static String safeError(Exception e) {
        if (e instanceof NullPointerException) {
            return "internal error — restart the game after updating the mod";
        }

        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return e.getClass().getSimpleName();
        }

        if (msg.toLowerCase().contains("cannot invoke")) {
            return "internal error — restart Minecraft";
        }

        if (msg.toLowerCase().contains("authentication failed")
                || msg.toLowerCase().contains("login failed")
                || msg.toLowerCase().contains("invalid twitch login")
                || msg.toLowerCase().contains("invalid credentials")) {
            return "login or OAuth token rejected by Twitch";
        }

        if (msg.toLowerCase().contains("oauth") || msg.toLowerCase().contains("token")) {
            return "check login (username) and OAuth token fields";
        }

        if (msg.toLowerCase().contains("login is empty")) {
            return "Twitch login missing";
        }

        if (msg.toLowerCase().contains("channel is empty")) {
            return "Twitch channel missing";
        }

        if (msg.toLowerCase().contains("token is missing")) {
            return "OAuth token missing";
        }

        return msg.length() > 80 ? msg.substring(0, 77) + "..." : msg;
    }
}
