package com.varlike.oathbreaker.core.mob;

import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.OathbreakerConfig;
import com.varlike.oathbreaker.core.attribute.AttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 全局怪物生命倍率。
 *
 * <p>目的不是做难度平衡，而是把生命数值放大到更大的区间，方便后续
 * 用配置/数据包做细调。新生成与从磁盘加载的怪物都会应用；已存在的怪物不重复应用。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class MobHealthScaling {
    private static final ResourceLocation HEALTH_MULTIPLIER_ID =
            AttributeModifiers.id("mob/health_multiplier");

    private MobHealthScaling() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        if (!(event.getEntity() instanceof Enemy) || !(event.getEntity() instanceof LivingEntity monster)) {
            return;
        }
        AttributeInstance maxHealth = monster.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null || maxHealth.hasModifier(HEALTH_MULTIPLIER_ID)) {
            return;
        }

        double oldMax = monster.getMaxHealth();
        float oldHealth = monster.getHealth();
        double multiplier = Math.max(1.0D, OathbreakerConfig.MOB_HEALTH_MULTIPLIER.get());
        AttributeModifiers.applyPermanent(monster, Attributes.MAX_HEALTH, HEALTH_MULTIPLIER_ID,
                multiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        float healthRatio = oldMax > 0.0D ? oldHealth / (float) oldMax : 1.0F;
        monster.setHealth(monster.getMaxHealth() * healthRatio);
        Oathbreaker.LOGGER.debug("Scaled mob health: {} x{} -> {}",
                monster.getType(), multiplier, monster.getMaxHealth());
    }
}
