package com.mickdev.tabchannel.Init;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.server.MinecraftServer;

/**
 * Serveurs hybrides (ex. Youer : NeoForge + Paper/Bukkit) : après enregistrement Brigadier,
 * il faut souvent pousser l’arbre de commandes vers le pont Bukkit pour que les joueurs
 * reçoivent la synchro client et que les plugins voient les commandes.
 */
public final class HybridCommandSupport {

    private HybridCommandSupport() {
    }

    public static void syncCommandsAfterRegistration(MinecraftServer server) {
        if (server == null) {
            return;
        }
        invokeNoArg(server, "syncCommands");
        invokeNoArg(server, "reloadCommands");
        invokeBukkitServerNoArg("syncCommands");
        invokeBukkitServerNoArg("reloadCommands");
        invokeBukkitServerNoArg("submitSyncCommandsAsync");
    }

    private static void invokeNoArg(Object target, String method) {
        if (target == null) {
            return;
        }
        try {
            Method m = target.getClass().getMethod(method);
            m.invoke(target);
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void invokeBukkitServerNoArg(String method) {
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object bs = bukkit.getMethod("getServer").invoke(null);
            invokeNoArg(bs, method);
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
