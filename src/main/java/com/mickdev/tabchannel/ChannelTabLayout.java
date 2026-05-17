package com.mickdev.tabchannel;

public final class ChannelTabLayout {

    public static final int TAB_HEIGHT = 20;
    public static final int BASE_TAB_WIDTH = 50;
    public static final int EXTRA_PER_CHAR = 6;

    private ChannelTabLayout() {
    }

    public static int computeWidth(String name) {
        int len = name == null ? 0 : name.length();
        if (len <= 5) {
            return BASE_TAB_WIDTH;
        }
        return BASE_TAB_WIDTH + (len - 5) * EXTRA_PER_CHAR;
    }
}