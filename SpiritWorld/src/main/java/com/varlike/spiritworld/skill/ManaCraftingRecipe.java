package com.varlike.spiritworld.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 魔力合成配方。
 *
 * @param id            唯一 id（用于存档）
 * @param output        产出物品（含数量）
 * @param manaCost      每种魔力类型的消耗
 * @param craftingTicks 合成时间（tick）
 */
public record ManaCraftingRecipe(
        ResourceLocation id,
        ItemStack output,
        Map<ManaType, Long> manaCost,
        int craftingTicks
) {}