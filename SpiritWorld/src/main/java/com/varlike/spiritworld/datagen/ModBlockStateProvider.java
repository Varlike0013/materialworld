package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.block.SpiritCollectorBlock;
import com.varlike.spiritworld.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SpiritWorld.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_BASIC, "spirit_collector_basic");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ADVANCED, "spirit_collector_advanced");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ULTIMATE, "spirit_collector_ultimate");
        simpleBlockWithItem(ModBlocks.MANA_BLOCK.get(),
                models().cubeAll("mana_block", ResourceLocation.withDefaultNamespace("block/mana_bricks")));
        slabBlock(ModBlocks.MANA_SLAB.get(),
                ResourceLocation.withDefaultNamespace("block/mana_bricks"),   // 双台阶模型
                ResourceLocation.withDefaultNamespace("block/mana_bricks"),   // side
                ResourceLocation.withDefaultNamespace("block/mana_bricks"),   // bottom
                ResourceLocation.withDefaultNamespace("block/mana_bricks"));  // top
        stairsBlock(ModBlocks.MANA_STAIRS.get(),
                ResourceLocation.withDefaultNamespace("block/mana_bricks"));
        simpleBlockWithItem(ModBlocks.SPIRIT_CORE.get(),
                models().orientableWithBottom(
                        "spirit_core",
                        modLoc("block/spirit_core_side"),    // 东、西、南
                        modLoc("block/spirit_core_front"),   // 北（正面）
                        modLoc("block/spirit_core_top"),     // 上
                        modLoc("block/spirit_core_bottom")   // 下
                ));
    }
    private void spiritCollector(DeferredBlock<SpiritCollectorBlock> block, String topTexture) {
        simpleBlockWithItem(block.get(), models().orientableWithBottom(
                block.getId().getPath(),
                modLoc("block/spirit_collector_side"),                                  // 侧面（东/西/南共用）
                modLoc("block/spirit_collector_front"),                                 // 正面（北）
                ResourceLocation.withDefaultNamespace("block/" + topTexture),           // 顶面：iron_block / gold_block / diamond_block
                modLoc("block/spirit_collector_bottom")                                 // 底面
        ));
    }
}