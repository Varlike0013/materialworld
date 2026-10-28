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
        // 改为引用我们新创建的自定义模型
        ModelFile level1 = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_level1"));
        ModelFile level2 = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_level2"));
        ModelFile full = new ModelFile.UncheckedModelFile(
                modLoc("block/spirit_cauldron_full"));

        getVariantBuilder(ModBlocks.SPIRIT_CAULDRON.get())
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 1)
                .modelForState().modelFile(level1).addModel()
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 2)
                .modelForState().modelFile(level2).addModel()
                .partialState()
                .with(LayeredCauldronBlock.LEVEL, 3)
                .modelForState().modelFile(full).addModel();
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_BASIC, "iron_block");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ADVANCED, "gold_block");
        spiritCollector(ModBlocks.SPIRIT_COLLECTOR_ULTIMATE, "diamond_block");
        simpleBlockWithItem(ModBlocks.MANA_BLOCK.get(),
                models().cubeAll("mana_block", ResourceLocation.withDefaultNamespace("block/purpur_block")));
        slabBlock(ModBlocks.MANA_SLAB.get(),
                ResourceLocation.withDefaultNamespace("block/purpur_block"),   // 双台阶模型
                ResourceLocation.withDefaultNamespace("block/purpur_block"),   // side
                ResourceLocation.withDefaultNamespace("block/purpur_block"),   // bottom
                ResourceLocation.withDefaultNamespace("block/purpur_block"));  // top
        stairsBlock(ModBlocks.MANA_STAIRS.get(),
                ResourceLocation.withDefaultNamespace("block/purpur_block"));
        simpleBlockWithItem(ModBlocks.SPIRIT_CORE.get(),
                models().cubeAll("spirit_core", ResourceLocation.withDefaultNamespace("block/amethyst_block")));
    }
    public void spiritCollector(DeferredBlock<SpiritCollectorBlock> block, String textureBlock) {
        simpleBlockWithItem(block.get(), models().cubeAll(
                block.getId().getPath(),
                ResourceLocation.withDefaultNamespace("block/" + textureBlock)));
    }
}