package com.mickdev.tabchannel;

import net.minecraft.server.level.ServerPlayer;

public enum ChannelTabColor {
    BLACK(0xFF000000, false),
    DARK_BLUE(0xFF0000AA, false),
    DARK_GREEN(0xFF00AA00, false),
    DARK_AQUA(0xFF00AAAA, false),
    DARK_RED(0xFFAA0000, false),
    DARK_PURPLE(0xFFAA00AA, false),
    GOLD(0xFFFFAA00, false),
    GRAY(0xFFAAAAAA, false),
    DARK_GRAY(0xFF555555, false),
    BLUE(0xFF5555FF, true),
    GREEN(0xFF55FF55, false),
    AQUA(0xFF55FFFF, false),
    RED(0xFFFF5555, true),
    LIGHT_PURPLE(0xFFFF55FF, false),
    YELLOW(0xFFFFFF55, false),
    WHITE(0xFFFFFFFF, false),

    ORANGE(0xFFFF8800, false),
    PINK(0xFFFF99CC, false),
    MAGENTA(0xFFFF00FF, false),
    CYAN(0xFF00E5FF, false),
    LIME(0xFF99FF00, false),
    BROWN(0xFF8B4513, false),
    SILVER(0xFFC0C0C0, false),
    NAVY(0xFF001F7F, false),
    TEAL(0xFF008080, false),
    OLIVE(0xFF808000, false),
    MAROON(0xFF800000, false),
    INDIGO(0xFF4B0082, false),
    VIOLET(0xFF8F00FF, false),
    TURQUOISE(0xFF40E0D0, false),
    SKY_BLUE(0xFF87CEEB, false),
    MINT(0xFF98FF98, false),
    SALMON(0xFFFA8072, false),
    CORAL(0xFFFF7F50, false),
    CRIMSON(0xFFDC143C, false),
    BEIGE(0xFFF5F5DC, false),
    IVORY(0xFFFFFFF0, false),
    CHARCOAL(0xFF36454F, false),
    EMERALD(0xFF50C878, false),
    RUBY(0xFFE0115F, false),
    AMBER(0xFFFFBF00, false),
    BRONZE(0xFFCD7F32, false),
    ROSE(0xFFFF007F, false),
    HOT_PINK(0xFFFF69B4, false),
    DEEP_PINK(0xFFFF1493, false),
    FUCHSIA(0xFFFF00AA, false),
    LAVENDER(0xFFE6E6FA, false),
    PLUM(0xFFDDA0DD, false),
    ORCHID(0xFFDA70D6, false),

    SCARLET(0xFFFF2400, false),
    FIRE_RED(0xFFB22222, false),
    BLOOD_RED(0xFF660000, false),
    CHERRY(0xFFDE3163, false),

    ICE_BLUE(0xFF99FFFF, false),
    BABY_BLUE(0xFF89CFF0, false),
    OCEAN_BLUE(0xFF1CA3EC, false),
    ROYAL_BLUE(0xFF4169E1, false),
    MIDNIGHT_BLUE(0xFF191970, false),

    FOREST_GREEN(0xFF228B22, false),
    DARK_FOREST(0xFF013220, false),
    MOSS_GREEN(0xFF8A9A5B, false),
    JADE(0xFF00A86B, false),
    NEON_GREEN(0xFF39FF14, true),

    SUNSET_ORANGE(0xFFFF4500, false),
    TANGERINE(0xFFFFA500, false),
    PEACH(0xFFFFCC99, false),
    APRICOT(0xFFFBCEB1, false),

    LEMON(0xFFFFF44F, false),
    CANARY(0xFFFFFF99, false),
    HONEY(0xFFFFC30B, false),

    COFFEE(0xFF6F4E37, false),
    CHOCOLATE(0xFF7B3F00, false),
    CARAMEL(0xFFFFD59A, false),
    SAND(0xFFC2B280, false),

    SNOW(0xFFFFFAFA, false),
    PEARL(0xFFEAE0C8, false),
    SMOKE(0xFF738276, false),
    ASH(0xFFB2BEB5, false),

    OBSIDIAN(0xFF0B0B0B, false),
    VOID(0xFF111111, true),
    NIGHT(0xFF0C1445, false),

    ELECTRIC_BLUE(0xFF7DF9FF, true),
    LASER_GREEN(0xFFCCFF00, true),
    TOXIC_GREEN(0xFF66FF00, true),
    RADIOACTIVE(0xFF7FFF00, true),

    GALAXY_PURPLE(0xFF6A0DAD, true),
    COSMIC_PINK(0xFFFF44CC, true),
    MANA_BLUE(0xFF3366FF, true),
    ENDER_PURPLE(0xFF4B006E, true),

    DIAMOND(0xFFB9F2FF, true),
    RUBY_RED(0xFF9B111E, true),
    SAPPHIRE(0xFF0F52BA, true),
    TOPAZ(0xFFFFC857, true),
    AMETHYST(0xFF9966CC, true),
    EMERALD_GREEN(0xFF50C878, true);

    private final int color;
    private final boolean adminOnly;

    ChannelTabColor(int color, boolean adminOnly) {
        this.color = color;
        this.adminOnly = adminOnly;
    }

    public int color() {
        return color;
    }

    public boolean adminOnly() {
        return adminOnly;
    }

    public static ChannelTabColor byName(String name) {
        if (name == null || name.isBlank()) {
            return WHITE;
        }

        for (ChannelTabColor color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }

        return WHITE;
    }

    /**
     * Couleur du libellé d’onglet : vert si sélectionné, sinon couleur du canal (défaut blanc).
     */
    public static int labelColor(String tabColorName, boolean selected, boolean hasUnread) {
        if (selected) {
            return GREEN.color();
        }
        if (hasUnread) {
            return 0xFF45F3FF;
        }
        return byName(tabColorName).color();
    }

    public static boolean canUse(ServerPlayer player, ChannelTabColor color) {
        if (color == null) {
            return false;
        }
        if (!color.adminOnly()) {
            return true;
        }
        return player.hasPermissions(2)
                || ChannelPermissionResolver.hasGlobalPerm(player, "tabchannel.admin");
    }
}