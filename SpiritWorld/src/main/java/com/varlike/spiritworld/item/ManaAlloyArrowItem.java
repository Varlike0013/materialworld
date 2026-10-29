package com.varlike.spiritworld.item;

import com.varlike.spiritworld.entity.ManaAlloyArrow;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaAlloyArrowItem extends ArrowItem {

    public static final double BASE_DAMAGE = 4.0;
    /** 力量附魔倍率 */
    private static final double POWER_MULTIPLIER = 1.5;

    public ManaAlloyArrowItem(Properties props) {
        super(props);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        ManaAlloyArrow arrow = new ManaAlloyArrow(level, shooter, ammo.copyWithCount(1), weapon);
        arrow.setBaseDamage(BASE_DAMAGE);

        if (weapon == null) return arrow;

        var reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        // ===== 力量 ×1.5 =====
        int powerLevel = weapon.getEnchantmentLevel(reg.getHolderOrThrow(Enchantments.POWER));
        if (powerLevel > 0) {
            double bonus = (powerLevel * 0.5 + 0.5) * POWER_MULTIPLIER;
            arrow.setBaseDamage(arrow.getBaseDamage() + bonus);
        }

        // ===== 冲击：原版等级存入 NBT，命中时手动施加 =====
        int punchLevel = weapon.getEnchantmentLevel(reg.getHolderOrThrow(Enchantments.PUNCH));
        if (punchLevel > 0) {
            CompoundTag data = arrow.getPersistentData();
            data.putInt("PunchLevel", punchLevel);
        }

        // ===== 火矢：标记，命中时手动设置燃烧 =====
        int flameLevel = weapon.getEnchantmentLevel(reg.getHolderOrThrow(Enchantments.FLAME));
        if (flameLevel > 0) {
            arrow.getPersistentData().putBoolean("Flame", true);
        }

        return arrow;
    }
}