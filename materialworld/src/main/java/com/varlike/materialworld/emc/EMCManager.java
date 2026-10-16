package com.varlike.materialworld.emc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * EMC（Energy-Matter Condensation，能量-物质凝聚）管理器。
 * 负责全局存储物品的物质量数值。
 */
public class EMCManager {

    // 核心映射表：Item -> EMC 值
    private static final Map<Item, Long> EMC_VALUES = new HashMap<>();

    /** 获取物品的 EMC 值，不考虑耐久,未定义时返回 0 */
    public static long getEMC(Item item) {
        return EMC_VALUES.getOrDefault(item, 0L);
    }

    /**
     * 有效值：按耐久度折算。
     * - 无耐久物品：返回基础值。
     * - 有耐久物品：基础值 * (剩余耐久 / 最大耐久)。
     * - 完全损坏（剩余耐久 = 0）：返回 0。
     */
    public static long getEMC(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0L;

        Item item = stack.getItem();
        long base = getEMC(item);
        if (base <= 0) return 0L;

        if (!stack.isDamageableItem()) {
            return base * stack.getCount();
        }

        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamageValue();
        if (remaining <= 0) return 0L;

        // 按剩余耐久比例折算，再乘以堆叠数量（工具通常不可堆叠，count=1）
        long perItem = base * remaining / max;
        return perItem * stack.getCount();
    }
    /** 有耐久物品的当前折算值（单个） */
    public static long getCurrentEMC(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0L;
        long base = getEMC(stack.getItem());
        if (base <= 0) return 0L;

        if (!stack.isDamageableItem()) return base;

        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamageValue();
        if (remaining <= 0) return 0L;
        return base * remaining / max;
    }

    /** 整栈价值：有耐久按折算×count，无耐久按基础×count */
    public static long getStackEMC(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0L;
        long unit = getCurrentEMC(stack);
        return unit * stack.getCount();
    }

    /** 设置或更新物品的 EMC 值 */
    public static void setEMC(Item item, long value) {
        if (item == null) return;
        if (value < 0) value = 0;
        EMC_VALUES.put(item, value);
    }

    /** 通过注册名（如 "minecraft:iron_ingot"）设置 EMC */
    public static void setEMC(String itemRegistryName, long value) {
        ResourceLocation rl = ResourceLocation.parse(itemRegistryName);
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            setEMC(item, value);
        }
    }

    /** 判断物品是否已定义 EMC */
    public static boolean hasEMC(Item item) {
        return EMC_VALUES.containsKey(item);
    }

    /** 获取已注册 EMC 的物品数量（用于调试） */
    public static int size() {
        return EMC_VALUES.size();
    }

    /** 清空所有值（用于重载） */
    public static void clear() {
        EMC_VALUES.clear();
    }

    /** 遍历所有已注册的 EMC 值（用于保存到文件） */
    public static void forEach(java.util.function.BiConsumer<Item, Long> action) {
        EMC_VALUES.forEach(action);
    }

    /** 移除某个物品的 EMC 定义 */
    public static void remove(Item item) {
        EMC_VALUES.remove(item);
    }
}