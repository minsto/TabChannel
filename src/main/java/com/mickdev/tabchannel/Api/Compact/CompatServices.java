package com.mickdev.tabchannel.Api.Compact;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatServices {

    public static final PermissionCompat PERMISSIONS = createPermissionCompat();
    public static final FactionCompat FACTIONS = createFactionCompat();
    public static final DiscordCompat DISCORD = createDiscordCompat();
    private CompatServices() {
    }

    private static PermissionCompat createPermissionCompat() {
        // LuckPerms MOD NeoForge/Forge
        if (FabricLoader.getInstance().isModLoaded("luckperms")) {
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


    private static DiscordCompat createDiscordCompat() {
        if (BukkitDetector.isBukkitPresent() && BukkitDetector.hasPlugin("DiscordSRV")) {
            return new BukkitDiscordSrvCompat();
        }

        return new NoDiscordCompat();
    }
    private static FactionCompat createFactionCompat() {
        // Mods factions NeoForge
      //  if (FabricLoader.getInstance().isModLoaded("odyssey_factions_rise")) {
        //    return new OdysseyFactionCompat();
       // }
        if (FabricLoader.getInstance().isModLoaded("soleaapi")) {
            return new SoleaFactionCompat();
        }
        if (FabricLoader.getInstance().isModLoaded("factions")) {
            return new FabricIckerFactionCompat();
        }
        //if (FabricLoader.getInstance().isModLoaded("factionwar")) {
          //  return new FactionWarCompat();
        //}

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