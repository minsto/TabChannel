package com.mickdev.tabchannel.Api.Compact;

import net.neoforged.fml.ModList;

public final class CompatServices {

    public static final PermissionCompat PERMISSIONS = createPermissionCompat();
    public static final FactionCompat FACTIONS = createFactionCompat();
    public static final DiscordCompat DISCORD = createDiscordCompat();

    private CompatServices() {
    }

    private static PermissionCompat createPermissionCompat() {
        if (ModList.get().isLoaded("luckperms")) {
            return new LuckPermsCompat();
        }

        if (BukkitDetector.isBukkitPresent()) {
            if (BukkitDetector.hasPlugin("LuckPerms")) {
                return new BukkitLuckPermsCompat();
            }

            if (BukkitDetector.hasPlugin("Vault")) {
                return new BukkitVaultCompat();
            }
        }

        return new NoPermissionCompat();
    }

    private static DiscordCompat createDiscordCompat() {
        if (BukkitDetector.isBukkitPresent()
                && BukkitDetector.hasPlugin("DiscordSRV")) {
            return new BukkitDiscordSrvCompat();
        }

        return new NoDiscordCompat();
    }

    private static FactionCompat createFactionCompat() {
        if (ModList.get().isLoaded("odyssey_factions_rise")) {
            return new OdysseyFactionCompat();
        }

        if (ModList.get().isLoaded("soleaapi")) {
            return new SoleaFactionCompat();
        }

        if (ModList.get().isLoaded("factionwar")) {
            return new FactionWarCompat();
        }

        if (BukkitDetector.isBukkitPresent()) {
            if (BukkitDetector.hasPlugin("Factions")
                    || BukkitDetector.hasPlugin("SaberFactions")
                    || BukkitDetector.hasPlugin("FactionsUUID")
                    || BukkitDetector.hasPlugin("MassiveCore")) {
                return new BukkitFactionsCompat();
            }
        }

        return new NoFactionCompat();
    }
}