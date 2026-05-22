package com.mickdev.tabchannel.Common.Mp;

public final class ClientMpAccess {

    private static boolean browseAllOnline;

    private ClientMpAccess() {
    }

    public static void setBrowseAllOnline(boolean value) {
        browseAllOnline = value;
    }

    public static boolean browsesAllOnlinePlayers() {
        return browseAllOnline;
    }

    public static void reset() {
        browseAllOnline = false;
    }
}
