package com.varlike.spiritworld.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 魔力合金工具材质：介于钻石和下界合金之间。
 * 耐久 1800（钻石 1561 / 下界合金 2031）
 * 速度 8.5（钻石 8 / 下界合金 9）
 * 攻击加成 3.5（钻石 3 / 下界合金 4）
 * 附魔性 12（钻石 10 / 下界合金 15）
 * 挖掘等级 = 钻石级（能挖黑曜石）
 */
public record ManaToolTier() implements Tier {

    public static final ManaToolTier INSTANCE = new ManaToolTier();

    @Override public int getUses()                          { return 1800; }
    @Override public float getSpeed()                       { return 8.5f; }
    @Override public float getAttackDamageBonus()           { return 3.5f; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
    @Override public int getEnchantmentValue()              { return 12; }
    @Override public Ingredient getRepairIngredient()       {
        return Ingredient.of(ModItems.MANA_ALLOY.get());
    }
}