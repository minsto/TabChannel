package com.mickdev.tabchannel.stream.twitch;

import com.mickdev.tabchannel.stream.StreamChatMessage;
import com.mickdev.tabchannel.stream.StreamPlatform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

final class TwitchIrcClient {

    private static final String HOST = "irc.chat.twitch.tv";
    private static final int PORT = 6667;
    private static final int HANDSHAKE_TIMEOUT_MS = 12_000;

    private final String channel;
    private final String oauthToken;
    private final String nick;

    private final BlockingQueue<StreamChatMessage> incoming = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean authenticated = new AtomicBoolean(false);

    private final Object writeLock = new Object();

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread readerThread;
    private volatile String lastError = "";

    TwitchIrcClient(String channel, String oauthToken, String nick) {
        this.channel = normalizeChannel(channel);
        this.oauthToken = normalizeToken(oauthToken);
        this.nick = nick == null || nick.isBlank() ? "justinfan12345" : nick.trim().toLowerCase(Locale.ROOT);
    }

    void connect() throws IOException {
        disconnect();
        lastError = "";

        socket = new Socket(HOST, PORT);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        sendRaw("CAP REQ :twitch.tv/tags twitch.tv/commands");
        sendRaw("PASS " + oauthToken);
        sendRaw("NICK " + nick);
        sendRaw("JOIN #" + channel);

        if (!performHandshake()) {
            disconnect();
            if (lastError.isBlank()) {
                lastError = "Twitch login failed — check login + OAuth token";
            }
            throw new IOException(lastError);
        }

        running.set(true);
        authenticated.set(true);

        readerThread = new Thread(() -> readLoop(), "TabChannel-TwitchIRC");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private boolean performHandshake() throws IOException {
        long deadline = System.currentTimeMillis() + HANDSHAKE_TIMEOUT_MS;
        boolean joined = false;

        while (System.currentTimeMillis() < deadline) {
            if (!reader.ready()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                continue;
            }

            String line = reader.readLine();
            if (line == null) {
                break;
            }

            if (handleServerLine(line, true)) {
                joined = true;
                break;
            }
        }

        return joined;
    }

    void disconnect() {
        running.set(false);
        authenticated.set(false);

        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        writer = null;
        reader = null;
        socket = null;
    }

    boolean isConnected() {
        return running.get() && authenticated.get() && socket != null && socket.isConnected() && !socket.isClosed();
    }

    String lastError() {
        return lastError;
    }

    void sendMessage(String text) throws IOException {
        if (!isConnected() || writer == null) {
            throw new IOException("Not connected to Twitch IRC");
        }

        sendRaw("PRIVMSG #" + channel + " :" + text.replace('\n', ' '));
    }

    List<StreamChatMessage> drainMessages() {
        List<StreamChatMessage> list = new ArrayList<>();
        incoming.drainTo(list);
        return list;
    }

    private void readLoop() {
        try {
            String line;

            while (running.get() && reader != null && (line = reader.readLine()) != null) {
                handleServerLine(line, false);
            }
        } catch (Exception e) {
            lastError = e.getMessage() == null ? "Twitch disconnected" : e.getMessage();
        } finally {
            running.set(false);
            authenticated.set(false);
        }
    }

    /** @return true when JOIN to target channel confirmed */
    private boolean handleServerLine(String line, boolean duringHandshake) {
        if (line == null || line.isBlank()) {
            return false;
        }

        String lower = line.toLowerCase(Locale.ROOT);

        if (line.startsWith("PING")) {
            try {
                sendRaw("PONG :tmi.twitch.tv");
            } catch (IOException e) {
                lastError = "Twitch connection lost";
                running.set(false);
            }
            return false;
        }

        if (lower.contains("login authentication failed") || lower.contains("login unsuccessful")
                || lower.contains("invalid nick") || lower.contains("improperly formatted auth")) {
            lastError = "Invalid Twitch login or OAuth token";
            running.set(false);
            authenticated.set(false);
            return false;
        }

        if (lower.contains("notice") && lower.contains("auth") && lower.contains("fail")) {
            lastError = "Twitch authentication failed";
            running.set(false);
            authenticated.set(false);
            return false;
        }

        if (lower.contains("notice") && (lower.contains("your message was not sent")
                || lower.contains("msg_ratelimit") || lower.contains("requires")
                || lower.contains("ban") || lower.contains("timeout"))) {
            lastError = noticeText(line);
            return false;
        }

        if (line.contains("JOIN #" + channel) || line.contains("JOIN #" + channel.toUpperCase(Locale.ROOT))) {
            if (line.contains(nick) || line.contains(":" + nick + "!")) {
                return duringHandshake;
            }
        }

        if (duringHandshake && (line.contains(" 001 ") || line.contains(" 376 ") || line.contains("GLOBALUSERSTATE"))) {
            return false;
        }

        StreamChatMessage message = parseLine(line);
        if (message != null) {
            incoming.offer(message);
        }

        return false;
    }

    private StreamChatMessage parseLine(String line) {
        if (!line.contains("PRIVMSG")) {
            return null;
        }

        String tags = "";
        String body = line;

        if (line.startsWith("@")) {
            int end = line.indexOf(' ');
            if (end > 0) {
                tags = line.substring(1, end);
                body = line.substring(end + 1);
            }
        }

        int privIdx = body.indexOf("PRIVMSG");
        if (privIdx < 0) {
            return null;
        }

        int colon = body.indexOf(':', privIdx);
        if (colon < 0 || colon + 1 >= body.length()) {
            return null;
        }

        String text = body.substring(colon + 1);
        String user = extractUser(body, tags);
        String display = extractTag(tags, "display-name", user);
        boolean mod = "1".equals(extractTag(tags, "mod", "0"));
        boolean sub = tags.contains("subscriber=1");
        boolean broadcaster = tags.contains("broadcaster/1") || tags.contains("badges=broadcaster");

        String badge = mod ? "[MOD]" : sub ? "[SUB]" : broadcaster ? "[OWNER]" : "";

        return new StreamChatMessage(
                StreamPlatform.TWITCH,
                user,
                display,
                text,
                0xFFB080FF,
                badge,
                System.currentTimeMillis(),
                mod,
                sub,
                broadcaster
        );
    }

    private static String extractUser(String body, String tags) {
        String display = extractTag(tags, "display-name", "");
        if (!display.isBlank()) {
            return display;
        }

        if (body.startsWith(":")) {
            int bang = body.indexOf('!');
            int end = bang > 0 ? bang : body.indexOf(' ');
            if (end > 1) {
                return body.substring(1, end);
            }
        }

        return "twitch";
    }

    private static String extractTag(String tags, String key, String fallback) {
        String needle = key + "=";
        int idx = tags.indexOf(needle);
        if (idx < 0) {
            return fallback;
        }

        int start = idx + needle.length();
        int end = tags.indexOf(';', start);
        String value = end < 0 ? tags.substring(start) : tags.substring(start, end);
        return value.replace("\"", "").trim();
    }

    private static String noticeText(String line) {
        int colon = line.indexOf(" :", line.indexOf("NOTICE"));
        if (colon >= 0 && colon + 2 < line.length()) {
            return line.substring(colon + 2).trim();
        }
        return "Twitch rejected the message";
    }

    private void sendRaw(String raw) throws IOException {
        synchronized (writeLock) {
            if (writer == null) {
                throw new IOException("Not connected to Twitch IRC");
            }

            writer.write(raw);
            writer.write("\r\n");
            writer.flush();
        }
    }

    private static String normalizeChannel(String channel) {
        if (channel == null) {
            return "";
        }

        return channel.trim().toLowerCase(Locale.ROOT).replace("#", "");
    }

    private static String normalizeToken(String token) {
        if (token == null) {
            return "";
        }

        String t = token.trim();
        if (!t.toLowerCase(Locale.ROOT).startsWith("oauth:")) {
            t = "oauth:" + t;
        }

        return t;
    }
}
