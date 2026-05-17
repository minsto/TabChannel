package com.mickdev.tabchannel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MENTION_STAFF_ADMIN_MAX = BUILDER
            .comment("Nombre max de mentions @staff / @admin / @mod par joueur dans la fenêtre (anti-spam).")
            .defineInRange("mentionStaffAdminMax", 3, 1, 3);

    public static final ModConfigSpec.IntValue MENTION_STAFF_ADMIN_WINDOW_SECONDS = BUILDER
            .comment("Durée en secondes de la fenêtre anti-spam pour les mentions staff/admin.")
            .defineInRange("mentionStaffAdminWindowSeconds", 60, 10, 3600);

    public static final ModConfigSpec.IntValue MENTION_STAFF_ADMIN_COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown en secondes après avoir dépassé le quota (les @staff/@admin ne notifient plus pendant ce délai, puis le quota se réinitialise).")
            .defineInRange("mentionStaffAdminCooldownSeconds", 30, 5, 600);

    static final ModConfigSpec SPEC = BUILDER.build();


}
