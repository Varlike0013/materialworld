package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.block.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SpiritWorld.MOD_ID);

    public static final DeferredBlock<SpiritCollectorBlock> SPIRIT_COLLECTOR_BASIC =
            BLOCKS.register("spirit_collector_basic", () -> new SpiritCollectorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL),
                    1L, 100L, 1));

    public static final DeferredBlock<SpiritCollectorBlock> SPIRIT_COLLECTOR_ADVANCED =
            BLOCKS.register("spirit_collector_advanced", () -> new SpiritCollectorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(4.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL),
                    2L, 500L, 2));

    public static final DeferredBlock<SpiritCollectorBlock> SPIRIT_COLLECTOR_ULTIMATE =
            BLOCKS.register("spirit_collector_ultimate", () -> new SpiritCollectorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .strength(5.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL),
                    4L, 2000L, 3));
    public static final DeferredBlock<ManaBlock> MANA_BLOCK =
            BLOCKS.register("mana_block", () -> new ManaBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops(),
                    20L));

    public static final DeferredBlock<ManaSlabBlock> MANA_SLAB =
            BLOCKS.register("mana_slab", () -> new ManaSlabBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops(),
                    3L));

    public static final DeferredBlock<ManaStairsBlock> MANA_STAIRS =
            BLOCKS.register("mana_stairs", () -> new ManaStairsBlock(
                    ModBlocks.MANA_BLOCK.get().defaultBlockState(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops(),
                    5L));

    public static final DeferredBlock<SpiritCoreBlock> SPIRIT_CORE =
            BLOCKS.register("spirit_core", () -> new SpiritCoreBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(5.0f)
                            .requiresCorrectToolForDrops()));
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}