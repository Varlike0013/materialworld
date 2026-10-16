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

    private static final Map<Item, Long> DEFAULTS = new HashMap<>();

    static {
        // ===== 自然方块 =====
        define(Items.COBBLESTONE, 1L);
        define(Items.DIRT, 1L);
        define(Items.COARSE_DIRT, 1L);
        define(Items.ROOTED_DIRT, 1L);
        define(Items.GRASS_BLOCK, 1L);
        define(Items.PODZOL, 1L);
        define(Items.MYCELIUM, 1L);
        define(Items.SAND, 1L);
        define(Items.RED_SAND, 1L);
        define(Items.GRAVEL, 1L);
        define(Items.STONE, 1L);
        define(Items.NETHERRACK, 1L);
        define(Items.END_STONE, 1L);
        define(Items.TUFF, 1L);
        define(Items.CALCITE, 1L);
        define(Items.BASALT, 1L);
        define(Items.BLACKSTONE, 1L);
        define(Items.DEEPSLATE, 1L);
        define(Items.COBBLED_DEEPSLATE, 1L);
        define(Items.GRANITE, 1L);
        define(Items.DIORITE, 1L);
        define(Items.ANDESITE, 1L);
        define(Items.OBSIDIAN, 64L);
        define(Items.CRYING_OBSIDIAN, 512L);
        define(Items.SOUL_SAND, 4L);
        define(Items.SOUL_SOIL, 4L);
        define(Items.MAGMA_BLOCK, 32L);

        // ===== 原木（所有种类）=====
        define(Items.OAK_LOG, 32L);
        define(Items.SPRUCE_LOG, 32L);
        define(Items.BIRCH_LOG, 32L);
        define(Items.JUNGLE_LOG, 32L);
        define(Items.ACACIA_LOG, 32L);
        define(Items.DARK_OAK_LOG, 32L);
        define(Items.MANGROVE_LOG, 32L);
        define(Items.CHERRY_LOG, 32L);
        define(Items.BAMBOO_BLOCK, 32L);
        define(Items.CRIMSON_STEM, 32L);
        define(Items.WARPED_STEM, 32L);

        // ===== 矿石掉落物 =====
        define(Items.COAL, 128L);
        define(Items.DIAMOND, 8192L);
        define(Items.EMERALD, 16384L);
        define(Items.LAPIS_LAZULI, 864L);
        define(Items.REDSTONE, 64L);
        define(Items.QUARTZ, 256L);
        define(Items.AMETHYST_SHARD, 64L);
        define(Items.RAW_IRON, 256L);
        define(Items.RAW_COPPER, 128L);
        define(Items.RAW_GOLD, 2048L);

        // ===== 熔炉产物（若推演不支持熔炉配方）=====
        define(Items.IRON_INGOT, 256L);
        define(Items.GOLD_INGOT, 2048L);
        define(Items.COPPER_INGOT, 128L);
        define(Items.CHARCOAL, 128L);
        define(Items.BRICK, 64L);
        define(Items.NETHER_BRICK, 64L);
        define(Items.GLASS, 1L);
        define(Items.NETHERITE_SCRAP, 34816L);
        define(Items.NETHERITE_INGOT, 139264L);

        // ===== 怪物掉落 =====
        define(Items.ENDER_PEARL, 1024L);
        define(Items.BLAZE_ROD, 1536L);
        define(Items.GHAST_TEAR, 4096L);
        define(Items.SHULKER_SHELL, 2048L);
        define(Items.PHANTOM_MEMBRANE, 2048L);
        define(Items.NAUTILUS_SHELL, 4096L);
        define(Items.NETHER_STAR, 139264L);
        define(Items.HEART_OF_THE_SEA, 32768L);
        define(Items.SLIME_BALL, 64L);
        define(Items.SPIDER_EYE, 64L);
        define(Items.BONE, 16L);
        define(Items.STRING, 16L);
        define(Items.GUNPOWDER, 64L);
        define(Items.LEATHER, 64L);
        define(Items.FEATHER, 16L);
        define(Items.INK_SAC, 16L);

        // ===== 植物 / 食物原料 =====
        define(Items.APPLE, 128L);
        define(Items.WHEAT_SEEDS, 16L);
        define(Items.WHEAT, 24L);
        define(Items.SUGAR_CANE, 32L);
        define(Items.CARROT, 64L);
        define(Items.POTATO, 64L);
        define(Items.BEETROOT, 64L);
        define(Items.PUMPKIN, 144L);
        define(Items.MELON_SLICE, 16L);
        define(Items.COCOA_BEANS, 32L);
        define(Items.SWEET_BERRIES, 16L);
        define(Items.GLOW_BERRIES, 16L);
        define(Items.KELP, 16L);
        define(Items.BAMBOO, 16L);
        define(Items.NETHER_WART, 64L);
        define(Items.CHORUS_FRUIT, 256L);

        // ===== 其他无配方物 =====
        define(Items.FLINT, 4L);
        define(Items.CLAY_BALL, 16L);
        define(Items.SNOWBALL, 1L);
        define(Items.ICE, 1L);
        define(Items.PACKED_ICE, 9L);
        define(Items.BLUE_ICE, 81L);
        define(Items.HONEYCOMB, 64L);
        define(Items.PRISMARINE_SHARD, 128L);
        define(Items.PRISMARINE_CRYSTALS, 512L);
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