package com.varlike.materialworld.emc;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * 原版物品的默认 EMC 值。
 * 保留一份快照，供 /emc reset 恢复使用。
 */
public class EMCValues {

    // 默认值快照：只在类加载时填充一次，之后不再改变
    private static final Map<Item, Long> DEFAULTS = new HashMap<>();

    static {
        // ===== 基础方块 =====
        define(Items.COBBLESTONE, 1L);
        define(Items.DIRT, 1L);
        define(Items.SAND, 1L);
        define(Items.GRAVEL, 1L);
        define(Items.STONE, 1L);
        define(Items.NETHERRACK, 1L);
        define(Items.END_STONE, 1L);

        // ===== 木头 =====
        define(Items.OAK_LOG, 32L);
        define(Items.OAK_PLANKS, 8L);
        define(Items.STICK, 4L);
        define(Items.COAL, 128L);
        define(Items.CHARCOAL, 128L);

        // ===== 金属 =====
        define(Items.IRON_INGOT, 256L);
        define(Items.GOLD_INGOT, 2048L);
        define(Items.COPPER_INGOT, 128L);
        define(Items.NETHERITE_INGOT, 139264L);

        // ===== 宝石 =====
        define(Items.DIAMOND, 8192L);
        define(Items.EMERALD, 16384L);
        define(Items.LAPIS_LAZULI, 864L);
        define(Items.REDSTONE, 64L);
        define(Items.QUARTZ, 256L);

        // ===== 稀有材料 =====
        define(Items.NETHER_STAR, 139264L);
        define(Items.ENDER_PEARL, 1024L);
        define(Items.BLAZE_ROD, 1536L);
        define(Items.GHAST_TEAR, 4096L);
        define(Items.SHULKER_SHELL, 2048L);

        // ===== 食物 =====
        define(Items.APPLE, 128L);
        define(Items.BREAD, 128L);
        define(Items.GOLDEN_APPLE, 18432L);
        define(Items.ENCHANTED_GOLDEN_APPLE, 147456L);
    }

    private static void define(Item item, long value) {
        DEFAULTS.put(item, value);
    }

    /** 把默认值写入 EMCManager（reload 时会重新调用） */
    public static void registerDefaults() {
        DEFAULTS.forEach(EMCManager::setEMC);
    }

    /** 获取某个物品的默认 EMC，未定义时返回 null */
    public static Long getDefault(Item item) {
        return DEFAULTS.get(item);
    }

    /** 判断物品是否有默认 EMC */
    public static boolean hasDefault(Item item) {
        return DEFAULTS.containsKey(item);
    }
}