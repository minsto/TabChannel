package com.mickdev.tabchannel.Api.Compact;

import net.neoforged.fml.ModList;

public final class CompatServices {

    public static final PermissionCompat PERMISSIONS = createPermissionCompat();
    public static final FactionCompat FACTIONS = createFactionCompat();

    private CompatServices() {
    }

    private static PermissionCompat createPermissionCompat() {
        // LuckPerms MOD NeoForge/Forge
        if (ModList.get().isLoaded("luckperms")) {
            return new LuckPermsCompat();
        }

        // Plugins Bukkit/Mohist
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

    private static FactionCompat createFactionCompat() {
        // Mods factions NeoForge
        if (ModList.get().isLoaded("odyssey_factions_rise")) {
            return new OdysseyFactionCompat();
        }
        if (ModList.get().isLoaded("soleaapi")) {
            return new SoleaFactionCompat();
        }
        if (ModList.get().isLoaded("factionwar")) {
            return new FactionWarCompat();
        }

        // Plugins factions Bukkit/Mohist
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