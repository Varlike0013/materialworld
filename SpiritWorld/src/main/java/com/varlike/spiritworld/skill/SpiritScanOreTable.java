package com.varlike.spiritworld.skill;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * 精神扫描的矿物表。
 * 只负责「扫描标识 → 方块/代表物品」的静态映射，不涉及扫描流程。
 *
 * 原版 8 种（煤/铁/金/红石/青金石/钻石/绿宝石/古代残骸）在静态块中注册；
 * 「其他模组矿物」在运行时从 c:ores 标签动态查询并排除原版 8 种。
 */
public final class SpiritScanOreTable {

    private SpiritScanOreTable() {}

    // ==================== 原版 8 种 ====================

    private static final Map<ResourceLocation, List<Block>> VANILLA_ORES = new HashMap<>();
    private static final Map<ResourceLocation, Item> REPRESENTATIVE_ITEMS = new HashMap<>();
    private static final Set<Block> ALL_VANILLA_BLOCKS = new HashSet<>();

    static {
        register(SkillValues.SCAN_COAL,
                List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
                Items.COAL);
        register(SkillValues.SCAN_IRON,
                List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
                Items.RAW_IRON);
        register(SkillValues.SCAN_GOLD,
                List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE),
                Items.RAW_GOLD);
        register(SkillValues.SCAN_REDSTONE,
                List.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
                Items.REDSTONE);
        register(SkillValues.SCAN_LAPIS,
                List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
                Items.LAPIS_LAZULI);
        register(SkillValues.SCAN_DIAMOND,
                List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
                Items.DIAMOND);
        register(SkillValues.SCAN_EMERALD,
                List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
                Items.EMERALD);
        register(SkillValues.SCAN_ANCIENT_DEBRIS,
                List.of(Blocks.ANCIENT_DEBRIS),
                Items.ANCIENT_DEBRIS);
    }

    private static void register(ResourceLocation oreId, List<Block> blocks, Item rep) {
        VANILLA_ORES.put(oreId, blocks);
        REPRESENTATIVE_ITEMS.put(oreId, rep);
        ALL_VANILLA_BLOCKS.addAll(blocks);
    }

    // ==================== 查询 API ====================

    /** 给定矿物标识返回方块列表，未知返回空 */
    public static List<Block> blocksOf(ResourceLocation oreId) {
        return VANILLA_ORES.getOrDefault(oreId, List.of());
    }

    /** 给定矿物标识返回代表物品，未知返回 null */
    public static Item representativeOf(ResourceLocation oreId) {
        return REPRESENTATIVE_ITEMS.get(oreId);
    }

    /** 判断方块是否属于原版 8 种矿物之一 */
    public static boolean isVanillaOre(Block block) {
        return ALL_VANILLA_BLOCKS.contains(block);
    }

    /**
     * 「其他模组矿物」：c:ores 标签中不属于原版 8 种的方块。
     * 每次调用动态查询（数据包重载会刷新标签），不缓存。
     */
    public static Map<Block, Item> collectOtherModOres() {
        Map<Block, Item> result = new HashMap<>();
        ResourceLocation tagId = ResourceLocation.fromNamespaceAndPath("c", "ores");
        for (Holder<Block> h : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.create(tagId))) {
            Block b = h.value();
            if (ALL_VANILLA_BLOCKS.contains(b)) continue;
            Item rep = b.asItem();
            if (rep != Items.AIR) result.put(b, rep);
        }
        return result;
    }
}