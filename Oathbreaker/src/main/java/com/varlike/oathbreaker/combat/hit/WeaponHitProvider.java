package com.varlike.oathbreaker.combat.hit;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 由自定义 Item 实现，让“继承/包装第三方武器”的物品直接提供命中效果，
 * 不需要额外调用 WeaponPool.register。
 *
 * <pre>{@code
 * public class MyBlade extends SwordItem implements WeaponHitProvider {
 *     @Override
 *     public HitEffect getWeaponHitEffect(ItemStack stack) {
 *         return MY_EFFECT;
 *     }
 * }
 * }</pre>
 */
public interface WeaponHitProvider {
    @Nullable
    HitEffect getWeaponHitEffect(ItemStack stack);
}
