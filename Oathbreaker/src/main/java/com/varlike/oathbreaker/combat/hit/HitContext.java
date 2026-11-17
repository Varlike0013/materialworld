package com.varlike.oathbreaker.combat.hit;

import com.varlike.oathbreaker.combat.DamageProfile;
import com.varlike.oathbreaker.core.weapon.WeaponDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 一次命中/一次伤害结算的上下文。
 *
 * <p>武器命中时由 {@code WeaponPool.fireHit} 构造；技能系统接入后
 * 用 {@link HitSource#SKILL} 构造同一对象，命中效果实现不需要区分来源。</p>
 */
public record HitContext(
        ServerPlayer attacker,
        LivingEntity target,
        ItemStack weapon,
        @Nullable WeaponDefinition weaponDefinition,
        DamageProfile damageProfile,
        float damageDealt,
        DamageSource source,
        HitSource hitSource
) {
}
