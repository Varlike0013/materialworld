package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class VisionFragmentEvents {

    /** 怪物 → 倍率。未列出的默认 1。 */
    private static final Map<EntityType<?>, Integer> MULTIPLIERS = Map.ofEntries(
            Map.entry(EntityType.ENDER_DRAGON, 100),
            Map.entry(EntityType.WITHER, 50),
            Map.entry(EntityType.WARDEN, 30),
            Map.entry(EntityType.ELDER_GUARDIAN, 20),
            Map.entry(EntityType.RAVAGER, 5),
            Map.entry(EntityType.EVOKER, 5),
            Map.entry(EntityType.ILLUSIONER, 5),
            Map.entry(EntityType.PIGLIN_BRUTE, 3),
            Map.entry(EntityType.HOGLIN, 3),
            Map.entry(EntityType.BLAZE, 3),
            Map.entry(EntityType.WITHER_SKELETON, 3),
            Map.entry(EntityType.ENDERMAN, 2),
            Map.entry(EntityType.SHULKER, 2),
            Map.entry(EntityType.GHAST, 2)
    );

    public static int getMultiplier(EntityType<?> type) {
        return MULTIPLIERS.getOrDefault(type, 1);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer) return;

        PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
        if (data == null) return;

        long base = getMultiplier(entity.getType()) + data.getVisionFragmentFlatBonus();
        double bonus = 1.0 + data.getVisionFragmentPercentBonus();
        int total = Math.max(0, (int) Math.round(base * bonus));

        if (total > 0) {
            data.addVisionFragments(total);
            NetworkHandler.syncTo(player);
        }
    }
}