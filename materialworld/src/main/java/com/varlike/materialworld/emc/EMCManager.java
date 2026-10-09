package com.varlike.materialworld.emc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * EMC（Energy-Matter Condensation，能量-物质凝聚）管理器。
 * 负责全局存储物品的物质量数值。
 */
public class EMCManager {

    // 核心映射表：Item -> EMC 值
    private static final Map<Item, Long> EMC_VALUES = new HashMap<>();

    /** 获取物品的 EMC 值，未定义时返回 0 */
    public static long getEMC(Item item) {
        return EMC_VALUES.getOrDefault(item, 0L);
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
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item != null) {
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