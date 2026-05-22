package com.mickdev.tabchannel.NetWork.CodecChanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientChannelTabState {

    public record TabEntry(
            String id,
            String displayName,
            boolean selected,
            boolean global,
            String tabColor,
            boolean staffChannel
    ) {
    }

    private static final List<TabEntry> TABS = new ArrayList<>();

    private static int page = 0;
    private static String selectedChannelId = "global";

    private ClientChannelTabState() {
    }

    public static void clear() {
        TABS.clear();
    }

    /** Déconnexion ou changement de monde côté client. */
    public static void resetForNewWorld() {
        TABS.clear();
        page = 0;
        selectedChannelId = "global";
    }

    public static void addTab(
            String id,
            String displayName,
            boolean global,
            boolean selected,
            int newPage,
            String tabColor,
            boolean staffChannel
    ) {
        String safeId = id == null || id.isBlank()
                ? "global"
                : id;

        String safeName = displayName == null || displayName.isBlank()
                ? safeId
                : displayName;

        String safeColor = tabColor == null || tabColor.isBlank()
                ? "GRAY"
                : tabColor;

        TABS.add(new TabEntry(
                safeId,
                safeName,
                selected,
                global,
                safeColor,
                staffChannel
        ));

        page = newPage;

        if (selected) {
            selectedChannelId = safeId;
        }
    }

    public static List<TabEntry> getTabs() {
        return Collections.unmodifiableList(TABS);
    }

    public static int getPage() {
        return page;
    }

    public static String getSelectedChannelId() {
        return selectedChannelId == null || selectedChannelId.isBlank()
                ? "global"
                : selectedChannelId;
    }

    public static void setSelectedChannelId(String id) {
        selectedChannelId = id == null || id.isBlank()
                ? "global"
                : id;

        List<TabEntry> copy = new ArrayList<>();

        for (TabEntry tab : TABS) {
            copy.add(new TabEntry(
                    tab.id(),
                    tab.displayName(),
                    tab.id().equals(selectedChannelId),
                    tab.global(),
                    tab.tabColor(),
                    tab.staffChannel()
            ));
        }

        TABS.clear();
        TABS.addAll(copy);
    }
}
