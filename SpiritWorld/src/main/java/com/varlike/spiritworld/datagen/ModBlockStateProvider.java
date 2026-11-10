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
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_BASIC,    "spirit_collector_basic");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ADVANCED, "spirit_collector_advanced");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ULTIMATE, "spirit_collector_ultimate");

        simpleBlockWithItem(ModBlocks.MANA_BLOCK.get(),
                models().cubeAll("mana_block", modLoc("block/mana_bricks")));

        slabBlock(ModBlocks.MANA_SLAB.get(),
                modLoc("block/mana_block"),    // 双台阶模型
                modLoc("block/mana_bricks"),
                modLoc("block/mana_bricks"),
                modLoc("block/mana_bricks"));

        stairsBlock(ModBlocks.MANA_STAIRS.get(),
                modLoc("block/mana_bricks"));

        simpleBlockWithItem(ModBlocks.SPIRIT_CORE.get(),
                models().orientableWithBottom(
                        "spirit_core",
                        modLoc("block/spirit_core_side"),
                        modLoc("block/spirit_core_front"),
                        modLoc("block/spirit_core_bottom"),  // bottom
                        modLoc("block/spirit_core_top")      // top
                ));
    }
    private void spiritCollector(DeferredBlock<SpiritCollectorBlock> block, String topTexture) {
        simpleBlockWithItem(block.get(), models().orientableWithBottom(
                block.getId().getPath(),
                modLoc("block/spirit_collector_side"),                          // side
                modLoc("block/spirit_collector_front"),                         // front
                modLoc("block/spirit_collector_bottom"),                        // bottom ← 第三个
                modLoc("block/" + topTexture)    // top    ← 第四个
        ));
    }
}