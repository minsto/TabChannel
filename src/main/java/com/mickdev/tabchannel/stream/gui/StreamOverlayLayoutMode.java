package com.mickdev.tabchannel.stream.gui;

public final class StreamOverlayLayoutMode {

    private StreamOverlayLayoutMode() {
    }

    public static void enterPosition() {
        StreamOverlayLayoutScheduler.openPosition();
    }

    public static void enterResize() {
        StreamOverlayLayoutScheduler.openResize();
    }
}
